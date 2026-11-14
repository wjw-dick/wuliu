package com.quantaida.wuliu.common.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 敏感字段加解密（AES-256-GCM）。密文以 enc: 前缀标识，未带前缀的历史数据按明文处理，便于平滑迁移。
 * 密钥通过环境变量 APP_AES_KEY 注入，缺省为演示密钥（生产必须替换）。
 */
public final class AppCipher {
    private static final String PREFIX = "enc:";
    private static final String DEFAULT_KEY = "wl-demo-aes-key-2026-quantaida!!";
    private static final byte[] KEY = normalize(System.getenv().getOrDefault("APP_AES_KEY", DEFAULT_KEY));
    private static final SecureRandom RANDOM = new SecureRandom();

    private AppCipher() {
    }

    private static byte[] normalize(String key) {
        byte[] raw = key.getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[32];
        for (int i = 0; i < 32; i++) {
            out[i] = raw.length == 0 ? 0 : raw[i % raw.length];
        }
        return out;
    }

    public static String encrypt(String plain) {
        if (plain == null || plain.isBlank() || plain.startsWith(PREFIX)) {
            return plain;
        }
        try {
            byte[] iv = new byte[12];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY, "AES"), new GCMParameterSpec(128, iv));
            byte[] cipherText = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] all = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, all, 0, iv.length);
            System.arraycopy(cipherText, 0, all, iv.length, cipherText.length);
            return PREFIX + Base64.getEncoder().encodeToString(all);
        } catch (Exception e) {
            throw new IllegalStateException("敏感字段加密失败", e);
        }
    }

    public static String decrypt(String value) {
        if (value == null || !value.startsWith(PREFIX)) {
            return value;
        }
        try {
            byte[] all = Base64.getDecoder().decode(value.substring(PREFIX.length()));
            byte[] iv = new byte[12];
            System.arraycopy(all, 0, iv, 0, 12);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(KEY, "AES"), new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(all, 12, all.length - 12), StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 演示数据或密钥轮换后无法解密时按原值返回，避免整页报错
            return value;
        }
    }

    public static String maskSecret(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        if (value.length() <= 4) {
            return "****";
        }
        return value.substring(0, Math.min(4, value.length() - 2)) + "******";
    }
}
