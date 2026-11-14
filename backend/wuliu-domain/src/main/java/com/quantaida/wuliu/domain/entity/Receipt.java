package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Receipt 实体（表 receipt） */
@Data
@TableName("receipt")
public class Receipt {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long waybillId;
    private String images;
    private String status;
    private String rejectReason;
    private String reviewer;
    private LocalDateTime reviewedAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}