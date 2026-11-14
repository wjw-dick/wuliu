package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Bill 实体（表 bill） */
@Data
@TableName("bill")
public class Bill {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String billNo;
    private String period;
    private BigDecimal seatFee;
    private BigDecimal extraFee;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private String status;
    private LocalDateTime paidAt;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}