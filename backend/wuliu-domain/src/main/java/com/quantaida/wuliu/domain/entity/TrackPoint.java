package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** TrackPoint 实体（表 track_point） */
@Data
@TableName("track_point")
public class TrackPoint {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long waybillId;
    private Long vehicleId;
    private BigDecimal lat;
    private BigDecimal lng;
    private BigDecimal speed;
    private LocalDateTime reportedAt;
    private String bucket;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}