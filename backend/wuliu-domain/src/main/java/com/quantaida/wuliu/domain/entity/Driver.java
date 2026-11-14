package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Driver 实体（表 driver） */
@Data
@TableName("driver")
public class Driver {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String name;
    @com.baomidou.mybatisplus.annotation.TableField(typeHandler = com.quantaida.wuliu.common.crypto.CryptoTypeHandler.class)
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.quantaida.wuliu.common.crypto.PhoneMaskSerializer.class)
    private String phone;
    private String licenseNo;
    private LocalDateTime licenseExpire;
    private String qualificationNo;
    private LocalDateTime qualificationExpire;
    private Long vehicleId;
    private String status;
    private Long userId;
    private Long carrierId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}