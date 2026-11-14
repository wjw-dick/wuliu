package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Receivable 实体（表 receivable） */
@Data
@TableName("receivable")
public class Receivable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long waybillId;
    private String waybillNo;
    private Long customerId;
    private String customerName;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private String status;
    private String source;
    private Long statementId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}