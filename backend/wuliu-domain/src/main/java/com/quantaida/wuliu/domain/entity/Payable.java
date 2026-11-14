package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Payable 实体（表 payable） */
@Data
@TableName("payable")
public class Payable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long waybillId;
    private String waybillNo;
    private Long carrierId;
    private Long driverId;
    private String payeeName;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private String status;
    private Long statementId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}