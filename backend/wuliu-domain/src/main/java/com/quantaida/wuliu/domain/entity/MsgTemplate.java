package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** MsgTemplate 实体（表 msg_template） */
@Data
@TableName("msg_template")
public class MsgTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String node;
    private String channel;
    private String templateId;
    private String title;
    private String content;
    private String smsChannel;
    private Integer enabled;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}