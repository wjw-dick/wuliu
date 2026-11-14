package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** ExceptionOrder 实体（表 exception_order） */
@Data
@TableName("exception_order")
public class ExceptionOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long waybillId;
    private String recordType;
    private String description;
    private String images;
    private String status;
    private String assignee;
    private String conclusion;
    private Integer escalated;
    private LocalDateTime handledAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}