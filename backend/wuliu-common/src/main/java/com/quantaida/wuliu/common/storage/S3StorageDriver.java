package com.quantaida.wuliu.common.storage;

import com.quantaida.wuliu.common.exception.BizException;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Map;

/** AWS S3 驱动，兼容 MinIO（endpoint 指向 MinIO 并开启 pathStyle）。 */
public class S3StorageDriver implements StorageDriver {
    private final S3Client client;
    private final S3Presigner presigner;
    private final String bucket;
    private final String driverName;

    public S3StorageDriver(Map<String, Object> config, String driverName) {
        String endpoint = str(config, "endpoint", "");
        String region = str(config, "region", "us-east-1");
        this.bucket = str(config, "bucket", "");
        this.driverName = driverName;
        AwsBasicCredentials credentials = AwsBasicCredentials.create(str(config, "accessKey", ""), str(config, "secretKey", ""));
        var builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials));
        if (!endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint)).forcePathStyle(true);
        }
        this.client = builder.build();
        var presignBuilder = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials));
        if (!endpoint.isBlank()) {
            presignBuilder.endpointOverride(URI.create(endpoint));
        }
        this.presigner = presignBuilder.build();
    }

    private static String str(Map<String, Object> c, String k, String def) {
        Object v = c.get(k);
        return v == null ? def : String.valueOf(v);
    }

    @Override
    public String driver() {
        return driverName;
    }

    @Override
    public void upload(String key, byte[] data, String contentType) {
        try {
            client.putObject(PutObjectRequest.builder().bucket(bucket).key(key).contentType(contentType).build(),
                    RequestBody.fromBytes(data));
        } catch (Exception e) {
            throw new BizException("对象存储上传失败：" + e.getMessage());
        }
    }

    @Override
    public InputStream download(String key) {
        return client.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build());
    }

    @Override
    public void delete(String key) {
        client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
    }

    @Override
    public String signedUrl(String key, int minutes) {
        return presigner.presignGetObject(GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(Math.min(minutes, 30)))
                        .getObjectRequest(GetObjectRequest.builder().bucket(bucket).key(key).build())
                        .build())
                .url().toString();
    }

    @Override
    public boolean test() {
        String key = "probe/" + System.currentTimeMillis() + ".txt";
        try {
            upload(key, "ok".getBytes(), "text/plain");
            boolean ok = client.headObject(b -> b.bucket(bucket).key(key)).sdkHttpResponse().isSuccessful();
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
