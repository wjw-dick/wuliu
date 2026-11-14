package com.quantaida.wuliu.service.system;

import com.quantaida.wuliu.common.context.TenantContext;
import com.quantaida.wuliu.domain.entity.AuditLog;
import com.quantaida.wuliu.domain.mapper.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** 操作审计：写库失败不影响主流程，仅记录日志。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogMapper auditLogMapper;

    public void log(String action, String target, String detail) {
        try {
            AuditLog log = new AuditLog();
            Long tenantId = TenantContext.tenantIdOrNull();
            log.setTenantId(tenantId == null ? 0L : tenantId);
            log.setScope(TenantContext.scope());
            log.setOperatorId(TenantContext.userId());
            log.setOperatorName(TenantContext.username());
            log.setAction(action);
            log.setTarget(target == null ? "" : target);
            log.setDetail(detail == null ? "" : detail);
            log.setIp("");
            auditLogMapper.insert(log);
        } catch (Exception e) {
            log.warn("审计日志写入失败：{}", e.getMessage());
        }
    }
}
