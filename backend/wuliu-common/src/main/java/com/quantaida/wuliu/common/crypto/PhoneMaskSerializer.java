package com.quantaida.wuliu.common.crypto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/** 手机号脱敏输出：138****0001；仅影响 JSON 展示，不影响库内密文。 */
public class PhoneMaskSerializer extends JsonSerializer<String> {
    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(mask(value));
    }

    public static String mask(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String plain = AppCipher.decrypt(value);
        if (plain.matches("^1\\d{10}$")) {
            return plain.substring(0, 3) + "****" + plain.substring(7);
        }
        if (plain.startsWith("enc:")) {
            return "****";
        }
        if (plain.length() > 6) {
            return plain.substring(0, 3) + "****" + plain.substring(plain.length() - 2);
        }
        return "****";
    }
}
