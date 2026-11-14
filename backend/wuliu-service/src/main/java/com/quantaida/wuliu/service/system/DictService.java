package com.quantaida.wuliu.service.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.wuliu.common.context.TenantContext;
import com.quantaida.wuliu.common.exception.BizException;
import com.quantaida.wuliu.common.web.BaseCrudController;
import com.quantaida.wuliu.domain.entity.DictItem;
import com.quantaida.wuliu.domain.entity.DictType;
import com.quantaida.wuliu.domain.mapper.DictItemMapper;
import com.quantaida.wuliu.domain.mapper.DictTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据字典服务：分类 + 字典项。中文名称必填、编码只读；
 * 翻译层统一提供 编码→中文名称，列表/详情/导出全部走此处。
 */
@Service
@RequiredArgsConstructor
public class DictService {
    private final DictTypeMapper typeMapper;
    private final DictItemMapper itemMapper;
    private final Map<String, Map<String, String>> cache = new ConcurrentHashMap<>();

    public List<DictType> types() {
        return typeMapper.selectList(new LambdaQueryWrapper<DictType>().orderByAsc(DictType::getSortNo));
    }

    public List<DictItem> items(String typeCode) {
        return itemMapper.selectList(new LambdaQueryWrapper<DictItem>()
                .eq(DictItem::getTypeCode, typeCode).orderByAsc(DictItem::getSortNo));
    }

    public Map<String, Object> all() {
        Map<String, Object> result = new LinkedHashMap<>();
        for (DictType type : types()) {
            result.put(type.getCode(), items(type.getCode()));
        }
        return result;
    }

    public String translate(String typeCode, String code) {
        if (code == null) return "";
        Long tenantId = TenantContext.tenantIdOrNull();
        String cacheKey = tenantId + ":" + typeCode;
        Map<String, String> map = cache.computeIfAbsent(cacheKey, k -> {
            Map<String, String> m = new ConcurrentHashMap<>();
            for (DictItem item : items(typeCode)) {
                m.put(item.getCode(), item.getName());
            }
            return m;
        });
        return map.getOrDefault(code, code);
    }

    public void evict() {
        cache.clear();
    }

    public void createItem(DictItem item) {
        if (item.getName() == null || item.getName().isBlank()) {
            throw new BizException("字典项中文名称必填");
        }
        if (item.getCode() == null || item.getCode().isBlank()) {
            throw new BizException("字典项编码由系统生成，不允许为空");
        }
        item.setId(null);
        itemMapper.insert(item);
        evict();
    }

    public void updateItem(Long id, DictItem item) {
        DictItem old = itemMapper.selectById(id);
        if (old == null) throw BizException.notFound("字典项不存在");
        if (item.getName() == null || item.getName().isBlank()) {
            throw new BizException("字典项中文名称必填");
        }
        old.setName(item.getName());
        old.setSortNo(item.getSortNo());
        old.setStatus(item.getStatus());
        // 编码只读：显式忽略入参 code
        itemMapper.updateById(old);
        evict();
    }

    public void deleteItem(Long id) {
        DictItem item = itemMapper.selectById(id);
        if (item != null && List.of("运单状态", "结算方式", "车型").contains(item.getTypeCode())) {
            throw new BizException("系统内置字典项不允许删除，可停用");
        }
        itemMapper.deleteById(id);
        evict();
    }

    public void createType(DictType type) {
        if (type.getName() == null || type.getName().isBlank()) throw new BizException("字典分类中文名称必填");
        if (type.getCode() == null || type.getCode().isBlank()) {
            type.setCode("D" + System.currentTimeMillis());
        }
        type.setId(null);
        typeMapper.insert(type);
        evict();
    }
}
