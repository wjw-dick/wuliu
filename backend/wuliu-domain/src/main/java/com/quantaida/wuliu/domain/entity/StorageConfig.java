package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** StorageConfig 实体（表 storage_config） */
@Data
@TableName("storage_config")
public class StorageConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String driver;
    private String configJson;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}