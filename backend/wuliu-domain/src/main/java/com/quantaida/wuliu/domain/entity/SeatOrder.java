package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** SeatOrder 实体（表 seat_order） */
@Data
@TableName("seat_order")
public class SeatOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Integer quantity;
    private BigDecimal amount;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}