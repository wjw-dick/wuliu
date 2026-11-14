package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** DictItem 实体（表 dict_item） */
@Data
@TableName("dict_item")
public class DictItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String typeCode;
    private String code;
    private String name;
    private Integer sortNo;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}