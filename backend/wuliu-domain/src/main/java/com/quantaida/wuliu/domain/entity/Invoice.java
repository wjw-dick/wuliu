package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Invoice 实体（表 invoice） */
@Data
@TableName("invoice")
public class Invoice {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String invoiceNo;
    private Long statementId;
    private Long customerId;
    private String invoiceType;
    private BigDecimal amount;
    private String status;
    private String appliedBy;
    private LocalDateTime issuedAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}