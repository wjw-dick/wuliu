package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Tenant 实体（表 tenant） */
@Data
@TableName("tenant")
public class Tenant {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String name;
    private String creditCode;
    private String industry;
    private String status;
    private Long planId;
    private Integer seatLimit;
    private Integer seatUsed;
    private LocalDateTime trialEnd;
    private LocalDateTime serviceStart;
    private LocalDateTime serviceEnd;
    private Integer networkMode;
    private String contactName;
    @com.baomidou.mybatisplus.annotation.TableField(typeHandler = com.quantaida.wuliu.common.crypto.CryptoTypeHandler.class)
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.quantaida.wuliu.common.crypto.PhoneMaskSerializer.class)
    private String contactPhone;
    private String contactEmail;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}