package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** I18nItem 实体（表 i18n_item） */
@Data
@TableName("i18n_item")
public class I18nItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String lang;
    private String keyName;
    private String valueText;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}