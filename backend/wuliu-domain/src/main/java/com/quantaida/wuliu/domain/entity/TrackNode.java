package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** TrackNode 实体（表 track_node） */
@Data
@TableName("track_node")
public class TrackNode {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long waybillId;
    private String node;
    private BigDecimal lat;
    private BigDecimal lng;
    private String operator;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}