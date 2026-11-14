package com.quantaida.wuliu.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** WechatAccount 实体（表 wechat_account） */
@Data
@TableName("wechat_account")
public class WechatAccount {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String accountType;
    private String appId;
    private String appSecret;
    private String serverUrl;
    private String token;
    private String aesKey;
    private String domains;
    private String ipWhitelist;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}