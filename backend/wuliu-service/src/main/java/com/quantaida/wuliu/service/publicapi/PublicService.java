package com.quantaida.wuliu.service.publicapi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantaida.wuliu.common.context.TenantContext;
import com.quantaida.wuliu.common.crypto.AppCipher;
import com.quantaida.wuliu.common.exception.BizException;
import com.quantaida.wuliu.common.security.RateLimiter;
import com.quantaida.wuliu.common.util.DangerousGoodsChecker;
import com.quantaida.wuliu.domain.entity.*;
import com.quantaida.wuliu.domain.mapper.*;
import com.quantaida.wuliu.service.pricing.PriceRequest;
import com.quantaida.wuliu.service.pricing.PricingService;
import com.quantaida.wuliu.service.system.NumberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/** 公开端（官网/小程序/H5）共用：询价、轨迹查询、留资、网点、资讯、月结申请。 */
@Service
@RequiredArgsConstructor
public class PublicService {
    private final QuoteMapper quoteMapper;
    private final WaybillMapper waybillMapper;
    private final TrackNodeMapper nodeMapper;
    private final TrackPointMapper pointMapper;
    private final SiteMapper siteMapper;
    private final CmsContentMapper contentMapper;
    private final LeadMapper leadMapper;
    private final PricingService pricingService;
    private final NumberService numberService;
    private final RateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    public Map<String, Object> quote(PriceRequest req, String clientKey) {
        rateLimiter.acquire("quote:" + clientKey, 30, 60);
        DangerousGoodsChecker.check(req.getCargoName(), req.getCargoType());
        Long tenantId = TenantContext.requireTenantId();
        Map<String, Object> price = pricingService.quote(req, tenantId, true);
        Quote quote = new Quote();
        quote.setQuoteNo(String.valueOf(price.get("quoteNo")));
        quote.setCustomerId(req.getCustomerId());
        quote.setOrigin(req.getOrigin());
        quote.setDestination(req.getDestination());
        quote.setCargoType(req.getCargoType());
        quote.setWeight(req.getWeight());
        quote.setVolume(req.getVolume());
        quote.setPieces(req.getPieces());
        quote.setVehicleType(req.getVehicleType());
        quote.setBaseFreight(dec(price.get("baseFreight")));
        quote.setFuelSurcharge(dec(price.get("fuelSurcharge")));
        quote.setLoadingFee(dec(price.get("loadingFee")));
        quote.setUpstairsFee(dec(price.get("upstairsFee")));
        quote.setInsuranceFee(dec(price.get("insuranceFee")));
        quote.setCodFee(dec(price.get("codFee")));
        quote.setRemoteFee(dec(price.get("remoteFee")));
        quote.setHolidayFee(dec(price.get("holidayFee")));
        quote.setTotalAmount(dec(price.get("totalAmount")));
        quote.setRuleVersion(String.valueOf(price.get("ruleVersion")));
        quote.setPriceSource(String.valueOf(price.get("priceSource")));
        quote.setDetailJson(toJson(price.get("detail")));
        quote.setSnapshotJson(toJson(price.get("snapshot")));
        quote.setStatus("待客户确认");
        quote.setApprovalStatus("无需审批");
        quote.setCreatedBy("官网/小程序");
        quote.setValidUntil(java.time.LocalDateTime.now().plusHours(48));
        quoteMapper.insert(quote);
        price.put("id", quote.getId());
        price.put("status", quote.getStatus());
        return price;
    }

    public Map<String, Object> track(String waybillNo, String phoneTail) {
        if (waybillNo == null || waybillNo.isBlank()) throw new BizException("请输入运单号");
        rateLimiter.acquire("track:" + waybillNo, 20, 60);
        Waybill wb = TenantContext.runWithoutTenant(() -> waybillMapper.selectOne(new LambdaQueryWrapper<Waybill>()
                .eq(Waybill::getWaybillNo, waybillNo).last("limit 1")));
        if (wb == null) throw BizException.notFound("运单号不存在，请核对后重试");
        if (phoneTail != null && phoneTail.length() >= 4) {
            String sender = AppCipher.decrypt(wb.getSenderPhone());
            String receiver = AppCipher.decrypt(wb.getReceiverPhone());
            boolean match = (sender != null && sender.endsWith(phoneTail)) || (receiver != null && receiver.endsWith(phoneTail));
            if (!match) throw new BizException(403, "手机号后四位与运单不匹配");
        }
        List<TrackNode> nodes = TenantContext.runAsReturn(wb.getTenantId(),
                () -> nodeMapper.selectList(new LambdaQueryWrapper<TrackNode>()
                        .eq(TrackNode::getWaybillId, wb.getId()).orderByAsc(TrackNode::getId)));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("waybillNo", wb.getWaybillNo());
        m.put("status", wb.getStatus());
        m.put("origin", wb.getOrigin());
        m.put("destination", wb.getDestination());
        m.put("senderName", wb.getSenderName());
        m.put("receiverName", wb.getReceiverName());
        m.put("cargoName", wb.getCargoName());
        m.put("pieces", wb.getPieces());
        m.put("pickupTime", wb.getPickupTime());
        m.put("signedAt", wb.getSignedAt());
        m.put("nodes", nodes);
        return m;
    }

    public void lead(String name, String phone, String company, String source, String remark) {
        if (name == null || name.isBlank()) throw new BizException("请填写联系人");
        if (phone == null || !phone.matches("^1\\d{10}$")) throw new BizException("请填写正确的手机号");
        rateLimiter.acquire("lead:" + phone, 5, 600);
        Lead lead = new Lead();
        lead.setName(name);
        lead.setPhone(phone);
        lead.setCompany(company);
        lead.setSource(source == null ? "官网询价" : source);
        lead.setRemark(remark);
        lead.setStatus("待分配");
        leadMapper.insert(lead);
    }

    public List<Site> sites() {
        return siteMapper.selectList(new LambdaQueryWrapper<Site>().eq(Site::getStatus, "营业中"));
    }

    public List<CmsContent> contents(String type) {
        return contentMapper.selectList(new LambdaQueryWrapper<CmsContent>()
                .eq(CmsContent::getStatus, "已发布")
                .eq(type != null && !type.isBlank(), CmsContent::getContentType, type)
                .orderByDesc(CmsContent::getPublishedAt));
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }

    private static java.math.BigDecimal dec(Object v) {
        if (v == null) return java.math.BigDecimal.ZERO;
        try {
            return new java.math.BigDecimal(String.valueOf(v));
        } catch (NumberFormatException e) {
            return java.math.BigDecimal.ZERO;
        }
    }
}
