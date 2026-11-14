package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** PlatformRole 实体（表 platform_role） */
@Data
@TableName("platform_role")
public class PlatformRole {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String menus;
    private String buttons;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}