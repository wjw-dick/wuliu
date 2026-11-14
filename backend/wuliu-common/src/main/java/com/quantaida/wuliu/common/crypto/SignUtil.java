package com.quantaida.wuliu.common.crypto;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

/** 本地文件签名与回调验签使用。 */
public final class SignUtil {
    private static final String KEY = System.getenv().getOrDefault("APP_SIGN_KEY", "wl-demo-sign-key-2026");

    private SignUtil() {
    }

    public static String sign(String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("签名计算失败", e);
        }
    }

    public static boolean verify(String content, String sign) {
        return sign != null && sign.equalsIgnoreCase(sign(content));
    }
}
