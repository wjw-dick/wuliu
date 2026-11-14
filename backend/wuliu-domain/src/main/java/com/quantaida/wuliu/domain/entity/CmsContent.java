package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** CmsContent 实体（表 cms_content） */
@Data
@TableName("cms_content")
public class CmsContent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String contentType;
    private String title;
    private String summary;
    private String content;
    private String cover;
    private String lang;
    private String status;
    private String seoTitle;
    private String seoKeywords;
    private LocalDateTime publishedAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}