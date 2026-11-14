package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** TenantLicense 实体（表 tenant_license） */
@Data
@TableName("tenant_license")
public class TenantLicense {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String licenseNo;
    private LocalDateTime licenseExpire;
    private String businessLicenseUrl;
    private String legalIdUrl;
    private String networkLicenseNo;
    private LocalDateTime networkLicenseExpire;
    private String status;
    private String rejectReason;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}