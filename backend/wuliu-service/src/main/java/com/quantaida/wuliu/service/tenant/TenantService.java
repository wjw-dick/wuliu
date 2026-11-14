package com.quantaida.wuliu.service.tenant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.wuliu.common.context.TenantContext;
import com.quantaida.wuliu.common.exception.BizException;
import com.quantaida.wuliu.domain.entity.Tenant;
import com.quantaida.wuliu.domain.entity.TenantLicense;
import com.quantaida.wuliu.domain.entity.TenantUser;
import com.quantaida.wuliu.domain.mapper.TenantLicenseMapper;
import com.quantaida.wuliu.domain.mapper.TenantMapper;
import com.quantaida.wuliu.domain.mapper.TenantUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** 租户生命周期五态：试用中 / 正常 / 欠费 / 停用 / 已到期，含到期定时转态与开单准入校验。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {
    private final TenantMapper tenantMapper;
    private final TenantLicenseMapper licenseMapper;
    private final TenantUserMapper userMapper;

    public Tenant current() {
        Long tenantId = TenantContext.requireTenantId();
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) throw BizException.forbidden("租户不存在");
        return tenant;
    }

    /** 开单准入：资质审核通过 + 租户状态允许（试用中/正常），欠费与到期只读。 */
    public void assertCanCreateWaybill() {
        Tenant tenant = current();
        if ("停用".equals(tenant.getStatus())) throw new BizException(403, "账号所属企业已停用，请联系服务商");
        if ("已到期".equals(tenant.getStatus())) throw new BizException(403, "服务已到期，仅可查看与导出数据，请续费后开单");
        if ("欠费".equals(tenant.getStatus())) throw new BizException(403, "企业已欠费，业务已暂停，请结清账单后开单");
        TenantLicense license = licenseMapper.selectOne(new LambdaQueryWrapper<TenantLicense>()
                .eq(TenantLicense::getTenantId, tenant.getId())
                .orderByDesc(TenantLicense::getId).last("limit 1"));
        if (license == null || !"已通过".equals(license.getStatus())) {
            throw new BizException(403, "企业资质尚未审核通过，暂不能生成运单");
        }
        if (license.getLicenseExpire() != null && license.getLicenseExpire().isBefore(LocalDateTime.now())) {
            throw new BizException(403, "《道路运输经营许可证》已过期，请更新资质后再开单");
        }
    }

    public void assertReadable() {
        Tenant tenant = current();
        if ("停用".equals(tenant.getStatus())) throw new BizException(403, "账号所属企业已停用，请联系服务商");
    }

    @Transactional
    public void assertSeatAvailable() {
        Tenant tenant = current();
        Long used = userMapper.selectCount(new LambdaQueryWrapper<TenantUser>()
                .eq(TenantUser::getTenantId, tenant.getId()).eq(TenantUser::getStatus, "正常"));
        int limit = tenant.getSeatLimit() == null ? 0 : tenant.getSeatLimit();
        if (limit > 0 && used >= limit) {
            throw new BizException(403, "席位已满（" + used + "/" + limit + "），请联系服务商增购");
        }
        tenant.setSeatUsed(used.intValue());
        tenantMapper.updateById(tenant);
    }

    @Transactional
    public Tenant renew(Long tenantId, int months) {
        if (months <= 0) throw new BizException("续期月数必须大于 0");
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) throw BizException.notFound("租户不存在");
        LocalDateTime base = tenant.getServiceEnd() != null && tenant.getServiceEnd().isAfter(LocalDateTime.now())
                ? tenant.getServiceEnd() : LocalDateTime.now();
        tenant.setServiceEnd(base.plusMonths(months));
        if (tenant.getServiceStart() == null) tenant.setServiceStart(LocalDateTime.now());
        tenant.setStatus("正常");
        tenantMapper.updateById(tenant);
        return tenant;
    }

    @Transactional
    public void suspend(Long tenantId, String reason) {
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) throw BizException.notFound("租户不存在");
        tenant.setStatus("停用");
        tenant.setRemark((tenant.getRemark() == null ? "" : tenant.getRemark()) + "；停用原因：" + (reason == null ? "未填写" : reason));
        tenantMapper.updateById(tenant);
    }

    public void enable(Long tenantId) {
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) throw BizException.notFound("租户不存在");
        boolean expired = tenant.getServiceEnd() != null && tenant.getServiceEnd().isBefore(LocalDateTime.now());
        tenant.setStatus(expired ? "已到期" : "正常");
        tenantMapper.updateById(tenant);
    }

    /** 每小时扫描：试用到期转已到期，服务期结束转已到期；到期 7 天后仍可只读导出。 */
    @Scheduled(cron = "0 10 * * * ?")
    public void lifecycleScan() {
        LocalDateTime now = LocalDateTime.now();
        List<Tenant> trials = tenantMapper.selectList(new LambdaQueryWrapper<Tenant>()
                .eq(Tenant::getStatus, "试用中").isNotNull(Tenant::getTrialEnd).lt(Tenant::getTrialEnd, now));
        for (Tenant t : trials) {
            t.setStatus("已到期");
            tenantMapper.updateById(t);
            log.info("租户 {} 试用到期，已转为已到期", t.getCode());
        }
        List<Tenant> actives = tenantMapper.selectList(new LambdaQueryWrapper<Tenant>()
                .in(Tenant::getStatus, "正常", "欠费").isNotNull(Tenant::getServiceEnd).lt(Tenant::getServiceEnd, now));
        for (Tenant t : actives) {
            t.setStatus("已到期");
            tenantMapper.updateById(t);
            log.info("租户 {} 服务到期，已转为已到期", t.getCode());
        }
    }
}
