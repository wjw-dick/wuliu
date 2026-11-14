package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Waybill 实体（表 waybill） */
@Data
@TableName("waybill")
public class Waybill {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String waybillNo;
    private Long customerId;
    private String customerName;
    private String senderName;
    @com.baomidou.mybatisplus.annotation.TableField(typeHandler = com.quantaida.wuliu.common.crypto.CryptoTypeHandler.class)
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.quantaida.wuliu.common.crypto.PhoneMaskSerializer.class)
    private String senderPhone;
    private String senderAddress;
    private String receiverName;
    @com.baomidou.mybatisplus.annotation.TableField(typeHandler = com.quantaida.wuliu.common.crypto.CryptoTypeHandler.class)
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.quantaida.wuliu.common.crypto.PhoneMaskSerializer.class)
    private String receiverPhone;
    private String receiverAddress;
    private String origin;
    private String destination;
    private String cargoName;
    private String cargoType;
    private BigDecimal weight;
    private BigDecimal actualWeight;
    private BigDecimal volume;
    private BigDecimal actualVolume;
    private Integer pieces;
    private String vehicleType;
    private String pickupType;
    private String deliveryType;
    private String settleType;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;
    private String status;
    private String quoteNo;
    private BigDecimal baseFreight;
    private BigDecimal fuelSurcharge;
    private BigDecimal loadingFee;
    private BigDecimal upstairsFee;
    private BigDecimal insuranceFee;
    private BigDecimal codFee;
    private BigDecimal remoteFee;
    private BigDecimal holidayFee;
    private BigDecimal totalAmount;
    private BigDecimal receivableAmount;
    private BigDecimal payableAmount;
    private BigDecimal grossProfit;
    private Integer insured;
    private BigDecimal codAmount;
    private String ruleVersion;
    private Long driverId;
    private String driverName;
    @com.baomidou.mybatisplus.annotation.TableField(typeHandler = com.quantaida.wuliu.common.crypto.CryptoTypeHandler.class)
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.quantaida.wuliu.common.crypto.PhoneMaskSerializer.class)
    private String driverPhone;
    private String plateNo;
    private Long carrierId;
    private String carrierName;
    private String source;
    private String remark;
    private String createdBy;
    private String repriceStatus;
    private BigDecimal repriceAmount;
    private LocalDateTime acceptedAt;
    private LocalDateTime signedAt;
    private LocalDateTime finishedAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}