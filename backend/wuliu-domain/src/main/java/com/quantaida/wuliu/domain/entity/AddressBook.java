package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** AddressBook 实体（表 address_book） */
@Data
@TableName("address_book")
public class AddressBook {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long customerId;
    private String name;
    @com.baomidou.mybatisplus.annotation.TableField(typeHandler = com.quantaida.wuliu.common.crypto.CryptoTypeHandler.class)
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.quantaida.wuliu.common.crypto.PhoneMaskSerializer.class)
    private String phone;
    private String region;
    private String detail;
    private String recordType;
    private Integer isDefault;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}