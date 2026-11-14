package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Quote 实体（表 quote） */
@Data
@TableName("quote")
public class Quote {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String quoteNo;
    private Long customerId;
    private String origin;
    private String destination;
    private String cargoType;
    private BigDecimal weight;
    private BigDecimal volume;
    private Integer pieces;
    private String vehicleType;
    private BigDecimal baseFreight;
    private BigDecimal fuelSurcharge;
    private BigDecimal loadingFee;
    private BigDecimal upstairsFee;
    private BigDecimal insuranceFee;
    private BigDecimal codFee;
    private BigDecimal remoteFee;
    private BigDecimal holidayFee;
    private BigDecimal totalAmount;
    private String ruleVersion;
    private String priceSource;
    private String detailJson;
    private String snapshotJson;
    private String status;
    private LocalDateTime validUntil;
    private String approvalStatus;
    private String createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}