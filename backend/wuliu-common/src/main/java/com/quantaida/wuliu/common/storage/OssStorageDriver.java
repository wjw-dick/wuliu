package com.quantaida.wuliu.common.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.quantaida.wuliu.common.exception.BizException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

/** 阿里云 OSS 驱动。 */
public class OssStorageDriver implements StorageDriver {
    private final OSS client;
    private final String endpoint;
    private final String bucket;

    public OssStorageDriver(Map<String, Object> config) {
        this.endpoint = String.valueOf(config.getOrDefault("endpoint", ""));
        this.bucket = String.valueOf(config.getOrDefault("bucket", ""));
        this.client = new OSSClientBuilder().build(endpoint,
                String.valueOf(config.getOrDefault("accessKey", "")),
                String.valueOf(config.getOrDefault("secretKey", "")));
    }

    @Override
    public String driver() {
        return "oss";
    }

    @Override
    public void upload(String key, byte[] data, String contentType) {
        try {
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentType(contentType);
            meta.setContentLength(data.length);
            client.putObject(bucket, key, new ByteArrayInputStream(data), meta);
        } catch (Exception e) {
            throw new BizException("OSS 上传失败：" + e.getMessage());
        }
    }

    @Override
    public InputStream download(String key) {
        return client.getObject(bucket, key).getObjectContent();
    }

    @Override
    public void delete(String key) {
        client.deleteObject(bucket, key);
    }

    @Override
    public String signedUrl(String key, int minutes) {
        Date expiration = new Date(System.currentTimeMillis() + Math.min(minutes, 30) * 60_000L);
        return client.generatePresignedUrl(bucket, key, expiration).toString();
    }

    @Override
    public boolean test() {
        String key = "probe/" + System.currentTimeMillis() + ".txt";
        try {
            upload(key, "ok".getBytes(), "text/plain");
            boolean ok = client.doesObjectExist(bucket, key);
            delete(key);
            return ok;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public long usedBytes() {
        return 0;
    }
}
