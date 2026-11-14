package com.quantaida.wuliu.common.storage;

import com.quantaida.wuliu.common.exception.BizException;

import java.util.Map;

/** 驱动工厂：后台选择驱动后动态构建，无需发版。 */
public final class StorageDrivers {
    private StorageDrivers() {
    }

    public static StorageDriver create(String driver, Map<String, Object> config) {
        if (driver == null) {
            throw new BizException("请选择对象存储驱动");
        }
        return switch (driver) {
            case "local" -> new LocalStorageDriver(config);
            case "s3" -> new S3StorageDriver(config, "s3");
            case "minio" -> new S3StorageDriver(config, "minio");
            case "oss" -> new OssStorageDriver(config);
            case "cos" -> new CosStorageDriver(config);
            default -> throw new BizException("不支持的对象存储驱动：" + driver);
        };
    }
}
