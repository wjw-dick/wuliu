package com.quantaida.wuliu.service.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.wuliu.common.context.TenantContext;
import com.quantaida.wuliu.common.crypto.AppCipher;
import com.quantaida.wuliu.common.exception.BizException;
import com.quantaida.wuliu.domain.entity.Carrier;
import com.quantaida.wuliu.domain.entity.Driver;
import com.quantaida.wuliu.domain.mapper.CarrierMapper;
import com.quantaida.wuliu.domain.mapper.DriverMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** 手机号加密存储，登录/公开查询需在内存中比对（仅限小结果集），不暴露密文。 */
@Service
@RequiredArgsConstructor
public class PhoneLookupService {
    private final DriverMapper driverMapper;
    private final CarrierMapper carrierMapper;

    public Driver findDriverByPhone(String phone) {
        if (phone == null) return null;
        List<Driver> drivers = TenantContext.runWithoutTenant(() -> driverMapper.selectList(new LambdaQueryWrapper<>()));
        return drivers.stream()
                .filter(d -> phone.equals(AppCipher.decrypt(d.getPhone())))
                .findFirst().orElse(null);
    }

    public Carrier findCarrierByPhone(String phone) {
        if (phone == null) return null;
        List<Carrier> carriers = TenantContext.runWithoutTenant(() -> carrierMapper.selectList(new LambdaQueryWrapper<>()));
        return carriers.stream()
                .filter(c -> phone.equals(AppCipher.decrypt(c.getContactPhone())))
                .findFirst().orElse(null);
    }

    public void assertNotBlank(String phone) {
        if (phone == null || phone.isBlank()) throw new BizException("请输入手机号");
    }
}
