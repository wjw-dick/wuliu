package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Agreement 实体（表 agreement） */
@Data
@TableName("agreement")
public class Agreement {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long customerId;
    private String origin;
    private String destination;
    private String vehicleType;
    private BigDecimal price;
    private BigDecimal minPrice;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private String status;
    private String approver;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}