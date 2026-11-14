package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Statement 实体（表 statement） */
@Data
@TableName("statement")
public class Statement {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String statementNo;
    private String recordType;
    private Long customerId;
    private Long carrierId;
    private String partyName;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private BigDecimal totalAmount;
    private BigDecimal confirmedAmount;
    private String status;
    private LocalDateTime confirmedAt;
    private String disputeReason;
    private String createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}