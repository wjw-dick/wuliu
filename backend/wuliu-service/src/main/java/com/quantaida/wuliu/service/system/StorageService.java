package com.quantaida.wuliu.service.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantaida.wuliu.common.context.TenantContext;
import com.quantaida.wuliu.common.crypto.AppCipher;
import com.quantaida.wuliu.common.exception.BizException;
import com.quantaida.wuliu.common.storage.StorageDriver;
import com.quantaida.wuliu.common.storage.StorageDrivers;
import com.quantaida.wuliu.domain.entity.StorageConfig;
import com.quantaida.wuliu.domain.mapper.StorageConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** 对象存储：5 驱动可切、参数加密、连接测试、按租户前缀分目录、签名 URL ≤ 30 分钟。 */
@Service
@RequiredArgsConstructor
public class StorageService {
    private static final Set<String> SECRET_KEYS = Set.of("secretKey", "accessKeySecret", "secretId", "sk");

    private final StorageConfigMapper configMapper;
    private final ObjectMapper objectMapper;
    private final Map<Long, DriverHolder> cache = new HashMap<>();

    private record DriverHolder(Long configId, StorageDriver driver) {
    }

    public Map<String, Object> current() {
        StorageConfig config = activeConfig();
        Map<String, Object> m = new LinkedHashMap<>();
        if (config == null) {
            m.put("driver", "local");
            m.put("status", "未配置");
            m.put("config", Map.of());
            return m;
        }
        Map<String, Object> cfg = parse(config.getConfigJson());
        cfg.replaceAll((k, v) -> SECRET_KEYS.contains(k) ? AppCipher.maskSecret(String.valueOf(v)) : v);
        m.put("id", config.getId());
        m.put("driver", config.getDriver());
        m.put("status", config.getStatus());
        m.put("config", cfg);
        m.put("drivers", List.of("local", "s3", "oss", "cos", "minio"));
        return m;
    }

    public void save(String driver, Map<String, Object> config) {
        if (driver == null || driver.isBlank()) throw new BizException("请选择存储驱动");
        StorageConfig old = activeConfig();
        StorageConfig target = new StorageConfig();
        target.setDriver(driver);
        Map<String, Object> merged = new LinkedHashMap<>(config == null ? Map.of() : config);
        if (old != null) {
            Map<String, Object> oldCfg = parse(old.getConfigJson());
            for (String key : SECRET_KEYS) {
                Object value = merged.get(key);
                if (value == null || String.valueOf(value).isBlank() || String.valueOf(value).contains("****")) {
                    merged.put(key, oldCfg.get(key));
                }
            }
        }
        for (String key : SECRET_KEYS) {
            Object v = merged.get(key);
            if (v != null) merged.put(key, AppCipher.encrypt(String.valueOf(v)));
        }
        try {
            target.setConfigJson(objectMapper.writeValueAsString(merged));
        } catch (Exception e) {
            throw new BizException("存储配置序列化失败");
        }
        target.setStatus("启用");
        if (old != null) {
            old.setStatus("停用");
            configMapper.updateById(old);
        }
        configMapper.insert(target);
        cache.clear();
    }

    public boolean test(String driver, Map<String, Object> config) {
        Map<String, Object> merged = new LinkedHashMap<>(config == null ? Map.of() : config);
        for (String key : SECRET_KEYS) {
            String v = merged.get(key) == null ? "" : String.valueOf(merged.get(key));
            if (v.contains("****")) {
                StorageConfig old = activeConfig();
                if (old != null) merged.put(key, parse(old.getConfigJson()).get(key));
            }
        }
        for (String key : SECRET_KEYS) {
            Object v = merged.get(key);
            if (v != null && !String.valueOf(v).startsWith("enc:")) merged.put(key, AppCipher.encrypt(String.valueOf(v)));
        }
        return StorageDrivers.create(driver, decrypt(merged)).test();
    }

    public Map<String, Object> upload(MultipartFile file, String module) {
        if (file == null || file.isEmpty()) throw new BizException("请选择要上传的文件");
        if (file.getSize() > 20 * 1024 * 1024) throw new BizException("单个文件不能超过 20MB");
        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
        String key = TenantContext.requireTenantId() + "/" + (module == null ? "common" : module) + "/"
                + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "/"
                + UUID.randomUUID().toString().replace("-", "") + ext;
        try {
            driver().upload(key, file.getBytes(), file.getContentType());
        } catch (Exception e) {
            throw new BizException("文件上传失败：" + e.getMessage());
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("key", key);
        result.put("url", driver().signedUrl(key, 30));
        result.put("name", original);
        result.put("size", file.getSize());
        return result;
    }

    public Map<String, Object> stats() {
        StorageDriver driver = driver();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("driver", driver.driver());
        m.put("usedBytes", driver.usedBytes());
        m.put("usedMb", Math.round(driver.usedBytes() / 1024.0 / 1024.0 * 100) / 100.0);
        m.put("signedUrlTtlMinutes", 30);
        return m;
    }

    public StorageDriver driver() {
        Long tenantId = TenantContext.requireTenantId();
        StorageConfig config = activeConfig();
        DriverHolder holder = cache.get(tenantId);
        if (holder != null && config != null && holder.configId().equals(config.getId())) {
            return holder.driver();
        }
        Map<String, Object> cfg = config == null ? Map.of("baseDir", "./data/storage") : decrypt(parse(config.getConfigJson()));
        String driverName = config == null ? "local" : config.getDriver();
        StorageDriver driver = StorageDrivers.create(driverName, cfg);
        if (config != null) cache.put(tenantId, new DriverHolder(config.getId(), driver));
        return driver;
    }

    public String signedUrl(String key) {
        return driver().signedUrl(key, 30);
    }

    public InputStream download(String key) {
        return driver().download(key);
    }

    private StorageConfig activeConfig() {
        return configMapper.selectOne(new LambdaQueryWrapper<StorageConfig>()
                .eq(StorageConfig::getStatus, "启用").orderByDesc(StorageConfig::getId).last("limit 1"));
    }

    private Map<String, Object> parse(String json) {
        if (json == null || json.isBlank()) return new LinkedHashMap<>();
        try {
            return objectMapper.readValue(json, new TypeReference<LinkedHashMap<String, Object>>() {
            });
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private Map<String, Object> decrypt(Map<String, Object> cfg) {
        Map<String, Object> copy = new LinkedHashMap<>(cfg);
        for (String key : SECRET_KEYS) {
            Object v = copy.get(key);
            if (v != null) copy.put(key, AppCipher.decrypt(String.valueOf(v)));
        }
        return copy;
    }
}
