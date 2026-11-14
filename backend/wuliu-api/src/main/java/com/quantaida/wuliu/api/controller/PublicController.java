package com.quantaida.wuliu.api.controller;

import com.quantaida.wuliu.common.exception.BizException;
import com.quantaida.wuliu.common.result.PageResult;
import com.quantaida.wuliu.domain.entity.CmsContent;
import com.quantaida.wuliu.domain.entity.Menu;
import com.quantaida.wuliu.domain.entity.Site;
import com.quantaida.wuliu.domain.mapper.CmsContentMapper;
import com.quantaida.wuliu.domain.mapper.MenuMapper;
import com.quantaida.wuliu.service.message.SmsGateway;
import com.quantaida.wuliu.service.pricing.PriceRequest;
import com.quantaida.wuliu.service.pricing.PricingService;
import com.quantaida.wuliu.service.publicapi.PublicService;
import com.quantaida.wuliu.service.system.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 公开端：官网/小程序/H5 共用的询价、轨迹、留资、网点、资讯。 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PublicController {
    private final PublicService publicService;
    private final MenuService menuService;
    private final CmsContentMapper contentMapper;
    private final MenuMapper menuMapper;
    private final PricingService pricingService;
    private final SmsGateway smsGateway;

    @PostMapping("/public/quote")
    public Map<String, Object> quote(@RequestBody PriceRequest req,
                                     @RequestHeader(value = "X-Client-Key", required = false) String key) {
        return publicService.quote(req, key == null ? "anonymous" : key);
    }

    @GetMapping("/public/track")
    public Map<String, Object> track(@RequestParam String waybillNo, @RequestParam(required = false) String phone) {
        return publicService.track(waybillNo, phone);
    }

    @GetMapping("/public/sites")
    public List<Site> sites() {
        return publicService.sites();
    }

    @PostMapping("/public/lead")
    public Map<String, Object> lead(@RequestBody Map<String, String> body) {
        publicService.lead(body.get("name"), body.get("phone"), body.get("company"),
                body.get("source"), body.get("remark"));
        return Map.of("submitted", true);
    }

    @PostMapping("/public/apply-monthly")
    public Map<String, Object> applyMonthly(@RequestBody Map<String, String> body) {
        publicService.lead(body.get("name"), body.get("phone"), body.get("company"),
                "企业月结申请", body.get("remark"));
        return Map.of("submitted", true);
    }

    @GetMapping("/public/contents")
    public List<CmsContent> contents(@RequestParam(required = false) String type) {
        return publicService.contents(type);
    }

    @GetMapping("/public/price-guide")
    public Map<String, Object> priceGuide() {
        return Map.of(
                "mode", "按重量体积取大",
                "volumeFactor", "体积重 = 体积(m³) × 250kg",
                "firstWeight", "首重 100kg 内一口价",
                "addWeight", "超出部分按 1.2 元/kg 计",
                "minPrice", "最低一票 200 元",
                "attachments", List.of("燃油附加 10%", "装卸费 50 元/票", "上楼费 30 元/票",
                        "保险费 货值×3‰", "代收手续费 代收金额×6‰", "偏远附加 20 元/票")
        );
    }

    @GetMapping("/public/nav")
    public List<Menu> nav(@RequestParam(defaultValue = "官网导航") String channel) {
        return menuService.channel(channel);
    }

    @GetMapping("/public/sms-channels")
    public List<String> smsChannels() {
        return smsGateway.channels();
    }

    @PostMapping("/public/pay/callback")
    public Map<String, Object> payCallback(@RequestBody Map<String, Object> body) {
        String sign = (String) body.get("sign");
        if (sign == null || !com.quantaida.wuliu.common.crypto.SignUtil.verify(
                String.valueOf(body.getOrDefault("orderNo", "")), sign)) {
            throw new BizException(401, "回调验签失败");
        }
        return Map.of("success", true);
    }

    @PostMapping("/wechat/callback")
    public Map<String, Object> wechatCallback(@RequestBody Map<String, Object> body) {
        return Map.of("echostr", body.getOrDefault("echostr", ""), "handled", true);
    }
}
