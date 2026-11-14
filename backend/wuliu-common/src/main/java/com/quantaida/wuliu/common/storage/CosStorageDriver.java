package com.quantaida.wuliu.common.storage;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.quantaida.wuliu.common.exception.BizException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

/** 腾讯云 COS 驱动。 */
public class CosStorageDriver implements StorageDriver {
    private final COSClient client;
    private final String bucket;

    public CosStorageDriver(Map<String, Object> config) {
        String region = String.valueOf(config.getOrDefault("region", "ap-guangzhou"));
        this.bucket = String.valueOf(config.getOrDefault("bucket", ""));
        COSCredentials credentials = new BasicCOSCredentials(
                String.valueOf(config.getOrDefault("accessKey", "")),
                String.valueOf(config.getOrDefault("secretKey", "")));
        this.client = new COSClient(credentials, new ClientConfig(new Region(region)));
    }

    @Override
    public String driver() {
        return "cos";
    }

    @Override
    public void upload(String key, byte[] data, String contentType) {
        try {
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentType(contentType);
            meta.setContentLength(data.length);
            client.putObject(new PutObjectRequest(bucket, key, new ByteArrayInputStream(data), meta));
        } catch (Exception e) {
            throw new BizException("COS 上传失败：" + e.getMessage());
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
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, key, HttpMethodName.GET);
        request.setExpiration(new Date(System.currentTimeMillis() + Math.min(minutes, 30) * 60_000L));
        return client.generatePresignedUrl(request).toString();
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
