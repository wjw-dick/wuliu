package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** StatementItem 实体（表 statement_item） */
@Data
@TableName("statement_item")
public class StatementItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long statementId;
    private Long waybillId;
    private String waybillNo;
    private BigDecimal amount;
    private BigDecimal difference;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}