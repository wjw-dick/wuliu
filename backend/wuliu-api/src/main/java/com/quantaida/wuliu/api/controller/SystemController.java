package com.quantaida.wuliu.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.wuliu.common.exception.BizException;
import com.quantaida.wuliu.domain.entity.*;
import com.quantaida.wuliu.domain.mapper.*;
import com.quantaida.wuliu.service.message.MessageService;
import com.quantaida.wuliu.service.message.SmsGateway;
import com.quantaida.wuliu.service.system.DictService;
import com.quantaida.wuliu.service.system.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/** 系统配置：参数、字典、消息、SSO、存储、公众号、多语言、CMS。 */
@RestController
@RequestMapping("/api/tenant/system")
@RequiredArgsConstructor
public class SystemController {
    private final SysConfigMapper configMapper;
    private final DictService dictService;
    private final MsgTemplateMapper templateMapper;
    private final MsgLogMapper msgLogMapper;
    private final MessageService messageService;
    private final SmsGateway smsGateway;
    private final SsoConfigMapper ssoMapper;
    private final StorageService storageService;
    private final WechatAccountMapper wechatMapper;
    private final I18nItemMapper i18nMapper;
    private final CmsContentMapper cmsMapper;

    @GetMapping("/configs")
    public List<SysConfig> configs() {
        return configMapper.selectList(new LambdaQueryWrapper<SysConfig>());
    }

    @PutMapping("/configs")
    public List<SysConfig> saveConfigs(@RequestBody List<SysConfig> configs) {
        for (SysConfig c : configs) {
            SysConfig old = configMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                    .eq(SysConfig::getConfigKey, c.getConfigKey()).last("limit 1"));
            if (old == null) {
                c.setId(null);
                configMapper.insert(c);
            } else {
                c.setId(old.getId());
                configMapper.updateById(c);
            }
        }
        return configs;
    }

    @GetMapping("/dicts")
    public Map<String, Object> dicts() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("types", dictService.types());
        m.put("all", dictService.all());
        return m;
    }

    @PostMapping("/dicts")
    public DictType createDictType(@RequestBody DictType type) {
        dictService.createType(type);
        return type;
    }

    @GetMapping("/dicts/{code}/items")
    public List<DictItem> items(@PathVariable String code) {
        return dictService.items(code);
    }

    @PostMapping("/dicts/{code}/items")
    public DictItem createItem(@PathVariable String code, @RequestBody DictItem item) {
        item.setTypeCode(code);
        item.setCode(item.getCode() == null || item.getCode().isBlank()
                ? "X" + System.currentTimeMillis() % 10000 : item.getCode());
        dictService.createItem(item);
        return item;
    }

    @PutMapping("/dicts/items/{id}")
    public DictItem updateItem(@PathVariable Long id, @RequestBody DictItem item) {
        dictService.updateItem(id, item);
        return item;
    }

    @DeleteMapping("/dicts/items/{id}")
    public Map<String, Object> deleteItem(@PathVariable Long id) {
        dictService.deleteItem(id);
        return Map.of("deleted", true);
    }

    @GetMapping("/message-templates")
    public List<MsgTemplate> templates() {
        return templateMapper.selectList(new LambdaQueryWrapper<MsgTemplate>());
    }

    @PostMapping("/message-templates")
    public MsgTemplate createTemplate(@RequestBody MsgTemplate template) {
        template.setId(null);
        if (template.getEnabled() == null) template.setEnabled(1);
        templateMapper.insert(template);
        return template;
    }

    @PutMapping("/message-templates/{id}")
    public MsgTemplate updateTemplate(@PathVariable Long id, @RequestBody MsgTemplate template) {
        template.setId(id);
        templateMapper.updateById(template);
        return templateMapper.selectById(id);
    }

    @DeleteMapping("/message-templates/{id}")
    public Map<String, Object> deleteTemplate(@PathVariable Long id) {
        templateMapper.deleteById(id);
        return Map.of("deleted", true);
    }

    @PostMapping("/message-templates/{id}/test")
    public Map<String, Object> testTemplate(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        MsgTemplate template = templateMapper.selectById(id);
        if (template == null) throw BizException.notFound("模板不存在");
        messageService.sendTest(template.getChannel(),
                String.valueOf(body.getOrDefault("receiver", "13800000000")), template.getContent());
        return Map.of("sent", true);
    }

    @GetMapping("/message-logs")
    public List<MsgLog> messageLogs() {
        return msgLogMapper.selectList(new LambdaQueryWrapper<MsgLog>().orderByDesc(MsgLog::getId).last("limit 200"));
    }

    @PostMapping("/message-logs/{id}/resend")
    public Map<String, Object> resend(@PathVariable Long id) {
        messageService.resend(id);
        return Map.of("resent", true);
    }

    @GetMapping("/sso")
    public SsoConfig sso() {
        return ssoMapper.selectOne(new LambdaQueryWrapper<SsoConfig>().orderByDesc(SsoConfig::getId).last("limit 1"));
    }

    @PutMapping("/sso")
    public SsoConfig saveSso(@RequestBody SsoConfig sso) {
        SsoConfig old = ssoMapper.selectOne(new LambdaQueryWrapper<SsoConfig>().orderByDesc(SsoConfig::getId).last("limit 1"));
        if (old == null) {
            sso.setId(null);
            ssoMapper.insert(sso);
        } else {
            sso.setId(old.getId());
            ssoMapper.updateById(sso);
        }
        return sso;
    }

    @GetMapping("/storage")
    public Map<String, Object> storage() {
        return storageService.current();
    }

    @PostMapping("/storage/test")
    public Map<String, Object> storageTest(@RequestBody Map<String, Object> body) {
        boolean ok = storageService.test(String.valueOf(body.get("driver")),
                (Map<String, Object>) body.getOrDefault("config", Map.of()));
        if (!ok) throw new BizException("连接测试失败，请检查参数");
        return Map.of("ok", true, "driver", body.get("driver"));
    }

    @PostMapping("/storage/apply")
    public Map<String, Object> storageApply(@RequestBody Map<String, Object> body) {
        storageService.save(String.valueOf(body.get("driver")),
                (Map<String, Object>) body.getOrDefault("config", Map.of()));
        return Map.of("applied", true);
    }

    @PostMapping("/storage/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file,
                                      @RequestParam(defaultValue = "common") String module) {
        return storageService.upload(file, module);
    }

    @GetMapping("/wechat")
    public List<WechatAccount> wechat() {
        return wechatMapper.selectList(new LambdaQueryWrapper<WechatAccount>());
    }

    @PostMapping("/wechat")
    public WechatAccount saveWechat(@RequestBody WechatAccount account) {
        account.setId(null);
        if (account.getStatus() == null) account.setStatus("正常");
        wechatMapper.insert(account);
        return account;
    }

    @GetMapping("/i18n")
    public List<I18nItem> i18n() {
        return i18nMapper.selectList(new LambdaQueryWrapper<I18nItem>());
    }

    @PostMapping("/i18n")
    public I18nItem createI18n(@RequestBody I18nItem item) {
        item.setId(null);
        i18nMapper.insert(item);
        return item;
    }

    @PutMapping("/i18n/{id}")
    public I18nItem updateI18n(@PathVariable Long id, @RequestBody I18nItem item) {
        item.setId(id);
        i18nMapper.updateById(item);
        return item;
    }

    @DeleteMapping("/i18n/{id}")
    public Map<String, Object> deleteI18n(@PathVariable Long id) {
        i18nMapper.deleteById(id);
        return Map.of("deleted", true);
    }

    @GetMapping("/cms/contents")
    public List<CmsContent> cmsContents(@RequestParam(required = false) String contentType) {
        return cmsMapper.selectList(new LambdaQueryWrapper<CmsContent>()
                .eq(contentType != null && !contentType.isBlank(), CmsContent::getContentType, contentType)
                .orderByDesc(CmsContent::getId));
    }

    @PostMapping("/cms/contents")
    public CmsContent createCms(@RequestBody CmsContent content) {
        content.setId(null);
        if (content.getStatus() == null) content.setStatus("草稿");
        if (content.getLang() == null) content.setLang("zh-CN");
        if (content.getPublishedAt() == null) content.setPublishedAt(new java.util.Date().toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        cmsMapper.insert(content);
        return content;
    }

    @PutMapping("/cms/contents/{id}")
    public CmsContent updateCms(@PathVariable Long id, @RequestBody CmsContent content) {
        content.setId(id);
        cmsMapper.updateById(content);
        return cmsMapper.selectById(id);
    }

    @DeleteMapping("/cms/contents/{id}")
    public Map<String, Object> deleteCms(@PathVariable Long id) {
        cmsMapper.deleteById(id);
        return Map.of("deleted", true);
    }
}
