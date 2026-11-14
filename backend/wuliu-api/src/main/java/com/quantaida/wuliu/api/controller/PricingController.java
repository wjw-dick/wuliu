package com.quantaida.wuliu.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quantaida.wuliu.common.context.TenantContext;
import com.quantaida.wuliu.common.exception.BizException;
import com.quantaida.wuliu.common.result.PageResult;
import com.quantaida.wuliu.domain.entity.Quote;
import com.quantaida.wuliu.domain.entity.PricingRule;
import com.quantaida.wuliu.domain.mapper.QuoteMapper;
import com.quantaida.wuliu.domain.mapper.PricingRuleMapper;
import com.quantaida.wuliu.service.order.WaybillService;
import com.quantaida.wuliu.service.pricing.PriceRequest;
import com.quantaida.wuliu.service.pricing.PricingService;
import com.quantaida.wuliu.service.system.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/** 计费：规则、试算、报价单、转下单、低于底价审批。 */
@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
public class PricingController {
    private final PricingService pricingService;
    private final PricingRuleMapper ruleMapper;
    private final QuoteMapper quoteMapper;
    private final WaybillService waybillService;
    private final AuditService auditService;

    @PostMapping("/pricing/quote")
    public Map<String, Object> quote(@RequestBody PriceRequest req) {
        req.setHoliday(Boolean.FALSE);
        return pricingService.quote(req, TenantContext.requireTenantId(), true);
    }

    @PostMapping("/pricing/simulate")
    public Map<String, Object> simulate(@RequestBody PriceRequest req) {
        return pricingService.quote(req, TenantContext.requireTenantId(), false);
    }

    @GetMapping("/pricing/rules")
    public List<PricingRule> rules() {
        return ruleMapper.selectList(new LambdaQueryWrapper<PricingRule>().orderByDesc(PricingRule::getId));
    }

    @PostMapping("/pricing/rules")
    public PricingRule createRule(@RequestBody PricingRule rule) {
        rule.setId(null);
        if (rule.getVersion() == null || rule.getVersion().isBlank()) rule.setVersion("V" + (ruleMapper.selectCount(null) + 1));
        if (rule.getStatus() == null) rule.setStatus("草稿");
        ruleMapper.insert(rule);
        return rule;
    }

    @PutMapping("/pricing/rules/{id}")
    public PricingRule updateRule(@PathVariable Long id, @RequestBody PricingRule rule) {
        rule.setId(id);
        ruleMapper.updateById(rule);
        return ruleMapper.selectById(id);
    }

    @PostMapping("/pricing/rules/{id}/publish")
    public PricingRule publish(@PathVariable Long id) {
        PricingRule rule = ruleMapper.selectById(id);
        if (rule == null) throw BizException.notFound("规则不存在");
        ruleMapper.selectList(new LambdaQueryWrapper<PricingRule>().eq(PricingRule::getStatus, "已生效"))
                .forEach(old -> {
                    old.setStatus("已停用");
                    ruleMapper.updateById(old);
                });
        rule.setStatus("已生效");
        rule.setEffectiveFrom(LocalDateTime.now());
        ruleMapper.updateById(rule);
        auditService.log("发布计费规则", rule.getVersion(), rule.getName());
        return rule;
    }

    @GetMapping("/quotes")
    public PageResult<Quote> quotes(@RequestParam Map<String, Object> params) {
        LambdaQueryWrapper<Quote> qw = new LambdaQueryWrapper<>();
        String status = params.get("status") == null ? "" : String.valueOf(params.get("status"));
        if (!status.isBlank()) qw.eq(Quote::getStatus, status);
        String keyword = params.get("keyword") == null ? "" : String.valueOf(params.get("keyword"));
        if (!keyword.isBlank()) qw.and(w -> w.like(Quote::getQuoteNo, keyword).or().like(Quote::getOrigin, keyword));
        qw.orderByDesc(Quote::getId);
        Page<Quote> page = quoteMapper.selectPage(new Page<>(parseLong(params.get("page"), 1),
                parseLong(params.get("size"), 10)), qw);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @PostMapping("/quotes")
    public Quote createQuote(@RequestBody Quote quote) {
        quote.setId(null);
        if (quote.getQuoteNo() == null || quote.getQuoteNo().isBlank()) {
            quote.setQuoteNo("BJ" + System.currentTimeMillis() % 100000);
        }
        if (quote.getStatus() == null) quote.setStatus("待客户确认");
        if (quote.getApprovalStatus() == null) quote.setApprovalStatus("无需审批");
        if (quote.getValidUntil() == null) quote.setValidUntil(LocalDateTime.now().plusHours(48));
        quoteMapper.insert(quote);
        return quote;
    }

    @PostMapping("/quotes/{id}/convert")
    public Map<String, Object> convert(@PathVariable Long id) {
        Quote quote = quoteMapper.selectById(id);
        if (quote == null) throw BizException.notFound("报价单不存在");
        if (!"待客户确认".equals(quote.getStatus())) {
            throw new BizException("报价单当前状态为「" + quote.getStatus() + "」，不能转下单");
        }
        var wb = new com.quantaida.wuliu.domain.entity.Waybill();
        wb.setCustomerId(quote.getCustomerId());
        wb.setOrigin(quote.getOrigin());
        wb.setDestination(quote.getDestination());
        wb.setCargoType(quote.getCargoType());
        wb.setWeight(quote.getWeight());
        wb.setVolume(quote.getVolume());
        wb.setPieces(quote.getPieces());
        wb.setVehicleType(quote.getVehicleType());
        wb.setQuoteNo(quote.getQuoteNo());
        wb.setSenderName("发货人");
        wb.setSenderPhone("13800000001");
        wb.setSenderAddress(quote.getOrigin() + "市示例路 1 号");
        wb.setReceiverName("收货人");
        wb.setReceiverPhone("13700000001");
        wb.setReceiverAddress(quote.getDestination() + "市示例路 2 号");
        wb.setCargoName("货物");
        wb.setPickupType("上门提货");
        wb.setDeliveryType("送货上门");
        wb.setSettleType("现结");
        wb.setLoadingFee(quote.getLoadingFee());
        wb.setUpstairsFee(quote.getUpstairsFee());
        wb.setCodAmount(quote.getCodFee());
        Map<String, Object> result = waybillService.create(wb, List.of(), List.of());
        quote.setStatus("已转化");
        quoteMapper.updateById(quote);
        return result;
    }

    @PostMapping("/quotes/{id}/approve")
    public Quote approve(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Quote quote = quoteMapper.selectById(id);
        if (quote == null) throw BizException.notFound("报价单不存在");
        boolean pass = Boolean.parseBoolean(String.valueOf(body.getOrDefault("pass", "false")));
        quote.setApprovalStatus(pass ? "已审批" : "已驳回");
        quoteMapper.updateById(quote);
        auditService.log("报价审批", quote.getQuoteNo(), pass ? "通过" : "驳回");
        return quote;
    }

    @PutMapping("/quotes/{id}")
    public Quote expire(@PathVariable Long id, @RequestBody Quote quote) {
        Quote old = quoteMapper.selectById(id);
        if (old == null) throw BizException.notFound("报价单不存在");
        old.setStatus(quote.getStatus() == null ? "已失效" : quote.getStatus());
        quoteMapper.updateById(old);
        return old;
    }

    @DeleteMapping("/quotes/{id}")
    public Map<String, Object> deleteQuote(@PathVariable Long id) {
        quoteMapper.deleteById(id);
        return Map.of("deleted", true);
    }

    private static long parseLong(Object v, long def) {
        try {
            return v == null ? def : Long.parseLong(String.valueOf(v));
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
