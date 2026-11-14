package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** RepairTask 实体（表 repair_task） */
@Data
@TableName("repair_task")
public class RepairTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String recordType;
    private Long waybillId;
    private String detail;
    private String status;
    private LocalDateTime handledAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}