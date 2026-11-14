package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** PlatformDict 实体（表 platform_dict） */
@Data
@TableName("platform_dict")
public class PlatformDict {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String name;
    private String itemsJson;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}