package com.quantaida.wuliu.common.storage;

import com.quantaida.wuliu.common.exception.BizException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

/** 本地磁盘驱动：私有化部署默认驱动，访问走带过期时间的签名地址。 */
public class LocalStorageDriver implements StorageDriver {
    private final Path baseDir;
    private final String publicBaseUrl;

    public LocalStorageDriver(Map<String, Object> config) {
        String dir = String.valueOf(config.getOrDefault("baseDir", "./data/storage"));
        this.baseDir = Paths.get(dir).toAbsolutePath().normalize();
        this.publicBaseUrl = String.valueOf(config.getOrDefault("publicBaseUrl", "/api/files"));
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new BizException("本地存储目录创建失败：" + e.getMessage());
        }
    }

    private Path resolve(String key) {
        Path p = baseDir.resolve(key).normalize();
        if (!p.startsWith(baseDir)) {
            throw new BizException(403, "非法的文件路径");
        }
        return p;
    }

    @Override
    public String driver() {
        return "local";
    }

    @Override
    public void upload(String key, byte[] data, String contentType) {
        Path target = resolve(key);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, data);
        } catch (IOException e) {
            throw new BizException("文件写入失败：" + e.getMessage());
        }
    }

    @Override
    public InputStream download(String key) {
        try {
            return Files.newInputStream(resolve(key));
        } catch (IOException e) {
            throw BizException.notFound("文件不存在或已删除");
        }
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException ignored) {
            // 删除幂等
        }
    }

    @Override
    public String signedUrl(String key, int minutes) {
        long expire = System.currentTimeMillis() / 1000 + Math.min(minutes, 30) * 60L;
        String sign = com.quantaida.wuliu.common.crypto.SignUtil.sign(key + ":" + expire);
        return publicBaseUrl + "/" + key + "?e=" + expire + "&s=" + sign;
    }

    @Override
    public boolean test() {
        String probe = "probe/" + System.currentTimeMillis() + ".txt";
        try {
            upload(probe, "ok".getBytes(), "text/plain");
            boolean exists = Files.exists(resolve(probe));
            delete(probe);
            return exists;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public long usedBytes() {
        try (Stream<Path> walk = Files.walk(baseDir)) {
            return walk.filter(Files::isRegularFile).mapToLong(p -> {
                try {
                    return Files.size(p);
                } catch (IOException e) {
                    return 0;
                }
            }).sum();
        } catch (IOException e) {
            return 0;
        }
    }

    public static void copy(InputStream in, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
    }
}
