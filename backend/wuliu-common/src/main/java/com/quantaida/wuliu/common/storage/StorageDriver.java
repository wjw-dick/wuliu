package com.quantaida.wuliu.common.storage;

import java.io.InputStream;

/** 对象存储统一驱动接口：本地 / S3 / 阿里云 OSS / 腾讯云 COS / MinIO。 */
public interface StorageDriver {
    String driver();

    void upload(String key, byte[] data, String contentType);

    InputStream download(String key);

    void delete(String key);

    /** 签名访问地址，有效期不得超过 30 分钟。 */
    String signedUrl(String key, int minutes);

    /** 连接测试：上传-读取-删除一个探针对象。 */
    boolean test();

    long usedBytes();
}
