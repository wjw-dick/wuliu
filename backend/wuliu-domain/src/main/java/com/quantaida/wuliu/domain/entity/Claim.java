package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Claim 实体（表 claim） */
@Data
@TableName("claim")
public class Claim {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String claimNo;
    private Long waybillId;
    private String waybillNo;
    private String recordType;
    private String description;
    private String evidence;
    private String liability;
    private String liabilityReason;
    private BigDecimal amount;
    private String status;
    private String rejectReason;
    private String handler;
    private LocalDateTime closedAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}