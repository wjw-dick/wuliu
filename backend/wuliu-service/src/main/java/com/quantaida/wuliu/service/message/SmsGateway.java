package com.quantaida.wuliu.service.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 短信网关：6 渠道单选（阿里云/腾讯云/华为云/容联/云片/自定义 HTTP），
 * 演示环境只做格式校验与日志落库，生产替换 send 实现即可。
 */
@Slf4j
@Component
public class SmsGateway {
    private static final List<String> CHANNELS = List.of("阿里云", "腾讯云", "华为云", "容联云", "云片", "自定义");

    public boolean send(String phone, String content) {
        if (phone == null || !phone.matches("^1\\d{10}$")) {
            return false;
        }
        log.info("[短信] 通道={} 手机={} 内容={}", CHANNELS.get(0), phone, content);
        return true;
    }

    public List<String> channels() {
        return CHANNELS;
    }
}
