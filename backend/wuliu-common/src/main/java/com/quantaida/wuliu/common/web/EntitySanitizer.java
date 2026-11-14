package com.quantaida.wuliu.common.web;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/** 编辑回显场景下前端会回传脱敏值（含 ****），入库前清空，避免把掩码写进库。 */
public final class EntitySanitizer {
    private EntitySanitizer() {
    }

    public static void clearMasked(Object entity) {
        if (entity == null) return;
        Class<?> clazz = entity.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field f : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || f.getType() != String.class) continue;
                try {
                    f.setAccessible(true);
                    Object v = f.get(entity);
                    if (v instanceof String s && s.contains("****")) {
                        f.set(entity, null);
                    }
                } catch (Exception ignored) {
                }
            }
            clazz = clazz.getSuperclass();
        }
    }
}
