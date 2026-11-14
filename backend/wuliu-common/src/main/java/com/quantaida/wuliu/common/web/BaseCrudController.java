package com.quantaida.wuliu.common.web;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.quantaida.wuliu.common.context.TenantContext;
import com.quantaida.wuliu.common.exception.BizException;
import com.quantaida.wuliu.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用 CRUD 基类：列表（关键字 + 等值 + 时间范围 + 分页）、详情、新增、修改、逻辑删除。
 * 子类自行声明 REST 路径（避免继承映射冲突），调用本类 protected 方法。
 * 所有查询由 MyBatis-Plus 多租户插件自动追加 tenant_id。
 */
public abstract class BaseCrudController<T> {
    private final BaseMapper<T> mapper;
    private final Class<T> entityClass;
    private final Map<String, String> columns = new LinkedHashMap<>();

    protected BaseCrudController(BaseMapper<T> mapper, Class<T> entityClass) {
        this.mapper = mapper;
        this.entityClass = entityClass;
        for (Field f : entityClass.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers())) {
                columns.put(f.getName(), camelToUnderline(f.getName()));
            }
        }
    }

    protected BaseMapper<T> mapper() {
        return mapper;
    }

    /** 关键字模糊匹配字段（实体属性名）。 */
    protected String[] keywordFields() {
        return new String[]{"name"};
    }

    protected void beforeSave(T entity, boolean create) {
    }

    public PageResult<T> list(Map<String, Object> params) {
        long pageNo = parseLong(params.get("page"), 1);
        long size = parseLong(params.get("size"), 10);
        QueryWrapper<T> qw = new QueryWrapper<>();
        List<String> kw = new ArrayList<>();
        for (String field : keywordFields()) {
            if (columns.containsKey(field)) kw.add(columns.get(field));
        }
        Object keyword = params.get("keyword");
        if (keyword != null && !String.valueOf(keyword).isBlank() && !kw.isEmpty()) {
            qw.and(w -> {
                for (int i = 0; i < kw.size(); i++) {
                    if (i == 0) w.like(kw.get(i), keyword);
                    else w.or().like(kw.get(i), keyword);
                }
            });
        }
        for (Map.Entry<String, Object> e : params.entrySet()) {
            String key = e.getKey();
            Object value = e.getValue();
            if (List.of("page", "size", "keyword", "sort", "order").contains(key)) continue;
            if (value == null || String.valueOf(value).isBlank()) continue;
            if ("startDate".equals(key) && columns.containsKey("createdAt")) {
                qw.ge(columns.get("createdAt"), value);
                continue;
            }
            if ("endDate".equals(key) && columns.containsKey("createdAt")) {
                qw.le(columns.get("createdAt"), value + " 23:59:59");
                continue;
            }
            if (key.endsWith("Like")) {
                String field = key.substring(0, key.length() - 4);
                if (columns.containsKey(field)) qw.like(columns.get(field), value);
                continue;
            }
            if (columns.containsKey(key)) {
                qw.eq(columns.get(key), value);
            }
        }
        String sort = String.valueOf(params.getOrDefault("sort", "id"));
        String order = "asc".equalsIgnoreCase(String.valueOf(params.getOrDefault("order", "desc"))) ? "asc" : "desc";
        qw.orderBy(true, !"desc".equals(order), columns.getOrDefault(sort, "id"));
        IPage<T> page = mapper.selectPage(new Page<>(pageNo, Math.min(size, 200)), qw);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    public T detail(Long id) {
        T entity = mapper.selectById(id);
        if (entity == null) throw BizException.notFound("记录不存在或已删除");
        return entity;
    }

    public T create(T entity) {
        beforeSave(entity, true);
        mapper.insert(entity);
        return entity;
    }

    public T update(Long id, T entity) {
        EntitySanitizer.clearMasked(entity);
        beforeSave(entity, false);
        setField(entity, "id", id);
        mapper.updateById(entity);
        return mapper.selectById(id);
    }

    public Map<String, Object> delete(Long id) {
        mapper.deleteById(id);
        return Map.of("id", id, "deleted", true);
    }
    protected static long parseLong(Object v, long def) {
        try {
            return v == null ? def : Long.parseLong(String.valueOf(v));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    protected static String camelToUnderline(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isUpperCase(c)) sb.append('_').append(Character.toLowerCase(c));
            else sb.append(c);
        }
        return sb.toString();
    }

    private void setField(T entity, String name, Object value) {
        try {
            Field f = entityClass.getDeclaredField(name);
            f.setAccessible(true);
            f.set(entity, value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    protected String currentOperator() {
        return TenantContext.username();
    }
}
