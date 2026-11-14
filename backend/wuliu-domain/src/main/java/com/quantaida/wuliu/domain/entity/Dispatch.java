package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Dispatch 实体（表 dispatch） */
@Data
@TableName("dispatch")
public class Dispatch {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String dispatchNo;
    private Long waybillId;
    private Long vehicleId;
    private Long driverId;
    private Long carrierId;
    private String status;
    private String source;
    private String assignee;
    private LocalDateTime assignedAt;
    private LocalDateTime acceptedAt;
    private BigDecimal emptyFee;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}