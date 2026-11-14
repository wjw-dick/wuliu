package com.quantaida.wuliu.service.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantaida.wuliu.common.exception.BizException;
import com.quantaida.wuliu.domain.entity.Menu;
import com.quantaida.wuliu.domain.mapper.MenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/** 菜单可视化：官网导航 / 小程序 tabBar / H5 底部导航 / 公众号菜单，同源配置、保存即生效。 */
@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuMapper menuMapper;
    private final ObjectMapper objectMapper;

    public List<Menu> list(String channel) {
        return menuMapper.selectList(new LambdaQueryWrapper<Menu>()
                .eq(channel != null && !channel.isBlank(), Menu::getChannel, channel)
                .orderByAsc(Menu::getSortNo));
    }

    public Map<String, Object> tree(String channel) {
        List<Menu> all = list(channel);
        List<Map<String, Object>> roots = new ArrayList<>();
        for (Menu m : all) {
            if (m.getParentId() == null || m.getParentId() == 0) {
                roots.add(toNode(m, all));
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("channel", channel);
        result.put("list", roots);
        return result;
    }

    private Map<String, Object> toNode(Menu m, List<Menu> all) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("id", m.getId());
        node.put("name", m.getName());
        node.put("path", m.getPath());
        node.put("icon", m.getIcon());
        node.put("sortNo", m.getSortNo());
        node.put("payload", m.getPayload());
        List<Map<String, Object>> children = new ArrayList<>();
        for (Menu child : all) {
            if (m.getId().equals(child.getParentId())) children.add(toNode(child, all));
        }
        if (!children.isEmpty()) node.put("children", children);
        return node;
    }

    @Transactional
    public void saveChannel(String channel, List<Map<String, Object>> items) {
        if (channel == null || channel.isBlank()) throw new BizException("请选择菜单渠道");
        menuMapper.delete(new LambdaQueryWrapper<Menu>().eq(Menu::getChannel, channel));
        int sort = 1;
        if (items != null) {
            for (Map<String, Object> item : items) {
                insert(channel, item, 0L, sort++);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void insert(String channel, Map<String, Object> item, Long parentId, int sort) {
        Menu menu = new Menu();
        menu.setChannel(channel);
        menu.setName(String.valueOf(item.getOrDefault("name", "未命名")));
        menu.setPath(String.valueOf(item.getOrDefault("path", "")));
        menu.setIcon(String.valueOf(item.getOrDefault("icon", "")));
        menu.setSortNo(item.get("sortNo") == null ? sort : Integer.parseInt(String.valueOf(item.get("sortNo"))));
        menu.setParentId(parentId);
        menu.setStatus("启用");
        try {
            menu.setPayload(objectMapper.writeValueAsString(item));
        } catch (Exception e) {
            menu.setPayload("{}");
        }
        menuMapper.insert(menu);
        Object children = item.get("children");
        if (children instanceof List<?> list) {
            int childSort = 1;
            for (Object child : list) {
                if (child instanceof Map<?, ?> map) {
                    insert(channel, (Map<String, Object>) map, menu.getId(), childSort++);
                }
            }
        }
    }

    public List<Menu> publicNav() {
        return list("官网导航");
    }

    public List<Menu> channel(String channel) {
        return list(channel);
    }
}
