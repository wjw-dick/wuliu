package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Vehicle 实体（表 vehicle） */
@Data
@TableName("vehicle")
public class Vehicle {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String plateNo;
    private String vehicleType;
    private BigDecimal tonnage;
    private BigDecimal volume;
    private String transportNo;
    private LocalDateTime transportExpire;
    private LocalDateTime insuranceExpire;
    private String fleet;
    private String status;
    private BigDecimal currentLat;
    private BigDecimal currentLng;
    private String currentStatus;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}