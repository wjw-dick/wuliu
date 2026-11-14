package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** SsoConfig 实体（表 sso_config） */
@Data
@TableName("sso_config")
public class SsoConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String provider;
    private String configJson;
    private Integer enabled;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}