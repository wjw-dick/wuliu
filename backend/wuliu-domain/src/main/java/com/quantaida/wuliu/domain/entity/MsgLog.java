package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** MsgLog 实体（表 msg_log） */
@Data
@TableName("msg_log")
public class MsgLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String node;
    private String channel;
    private String receiver;
    private String content;
    private String status;
    private String errorMsg;
    private Integer retryCount;
    private Long waybillId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}