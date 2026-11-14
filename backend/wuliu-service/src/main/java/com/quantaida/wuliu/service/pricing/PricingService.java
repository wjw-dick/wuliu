package com.quantaida.wuliu.service.pricing;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantaida.wuliu.common.exception.BizException;
import com.quantaida.wuliu.domain.entity.Agreement;
import com.quantaida.wuliu.domain.entity.PricingRule;
import com.quantaida.wuliu.domain.mapper.AgreementMapper;
import com.quantaida.wuliu.domain.mapper.PricingRuleMapper;
import com.quantaida.wuliu.service.system.NumberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 计费引擎：协议价 > 线路标准价 > 默认规则；支持首重续重/按公斤/按方/取大/按公里/按票/最低一票/包车，
 * 附加费（燃油/装卸/上楼/保险/代收/偏远/节假日）全部后台可配，报价落快照。
 */
@Service
@RequiredArgsConstructor
public class PricingService {
    private final PricingRuleMapper ruleMapper;
    private final AgreementMapper agreementMapper;
    private final NumberService numberService;
    private final ObjectMapper objectMapper;

    public Map<String, Object> quote(PriceRequest req, Long tenantId, boolean persistNumber) {
        BigDecimal weight = nz(req.getWeight());
        BigDecimal volume = nz(req.getVolume());
        if (weight.compareTo(BigDecimal.ZERO) <= 0 && volume.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("请填写货物重量或体积");
        }
        if (req.getOrigin() == null || req.getDestination() == null) {
            throw new BizException("请选择出发地与目的地");
        }
        PricingRule rule = activeRule();
        Map<String, Object> config = parse(rule == null ? null : rule.getConfigJson());
        BigDecimal volumeWeight = volume.multiply(dec(config, "volumeFactor", new BigDecimal("250")));
        BigDecimal chargeWeight = weight.max(volumeWeight);
        String mode = str(config, "mode", "按重量体积取大");

        BigDecimal base = BigDecimal.ZERO;
        String source = "默认规则";
        String formula = "";

        Agreement agreement = matchAgreement(req);
        if (agreement != null) {
            base = nz(agreement.getPrice());
            source = "协议价";
            formula = "协议价（" + agreement.getOrigin() + " → " + agreement.getDestination() + "，"
                    + agreement.getVehicleType() + "）";
        } else {
            BigDecimal routePrice = matchRoute(config, req);
            if (routePrice != null) {
                base = routePrice;
                source = "线路标准价";
                formula = "线路标准价（" + req.getOrigin() + " → " + req.getDestination() + "）";
            } else {
                switch (mode) {
                    case "按体积" -> {
                        base = volume.multiply(dec(config, "pricePerCbm", new BigDecimal("300")));
                        formula = "体积 " + volume.stripTrailingZeros().toPlainString() + "m³ × " +
                                dec(config, "pricePerCbm", new BigDecimal("300")).toPlainString() + " 元/m³";
                    }
                    case "按公里" -> {
                        BigDecimal km = distance(config, req);
                        base = km.multiply(dec(config, "pricePerKm", new BigDecimal("6")));
                        formula = "里程 " + km.stripTrailingZeros().toPlainString() + "km × " +
                                dec(config, "pricePerKm", new BigDecimal("6")).toPlainString() + " 元/km";
                    }
                    case "按票" -> {
                        base = dec(config, "pricePerTicket", new BigDecimal("300"));
                        formula = "按票计费";
                    }
                    case "包车" -> {
                        base = dec(config, "charterPrice", new BigDecimal("1800"));
                        formula = "包车价（" + req.getVehicleType() + "）";
                    }
                    default -> {
                        // 首重续重 + 取大（按重量体积取大时同时比较体积价）
                        BigDecimal firstWeight = dec(config, "firstWeight", new BigDecimal("100"));
                        BigDecimal firstPrice = dec(config, "firstPrice", new BigDecimal("120"));
                        BigDecimal addPrice = dec(config, "addWeightPrice", new BigDecimal("1.2"));
                        if (chargeWeight.compareTo(firstWeight) <= 0) {
                            base = firstPrice;
                            formula = "首重 " + firstWeight.stripTrailingZeros().toPlainString() + "kg / "
                                    + firstPrice.toPlainString() + " 元";
                        } else {
                            BigDecimal excess = chargeWeight.subtract(firstWeight);
                            base = firstPrice.add(excess.multiply(addPrice));
                            formula = "首重 " + firstWeight.stripTrailingZeros().toPlainString() + "kg / " + firstPrice.toPlainString()
                                    + " 元 + 续重 " + excess.stripTrailingZeros().toPlainString() + "kg × " + addPrice.toPlainString() + " 元/kg";
                        }
                        if ("按重量体积取大".equals(mode) && volumeWeight.compareTo(weight) > 0) {
                            BigDecimal volumePrice = volume.multiply(dec(config, "pricePerCbm", new BigDecimal("300")));
                            if (volumePrice.compareTo(base) > 0) {
                                base = volumePrice;
                                formula = "体积重 " + volumeWeight.stripTrailingZeros().toPlainString() + "kg 取大（" +
                                        volume.stripTrailingZeros().toPlainString() + "m³ × " +
                                        dec(config, "pricePerCbm", new BigDecimal("300")).toPlainString() + " 元/m³）";
                            }
                        }
                    }
                }
                base = base.max(dec(config, "minPrice", new BigDecimal("200")));
            }
        }

        BigDecimal fuel = base.multiply(dec(config, "fuelRate", new BigDecimal("0.10")));
        BigDecimal loading = Boolean.TRUE.equals(req.getNeedLoading()) ? dec(config, "loadingFee", new BigDecimal("50")) : BigDecimal.ZERO;
        BigDecimal upstairs = Boolean.TRUE.equals(req.getNeedUpstairs()) ? dec(config, "upstairsFee", new BigDecimal("30")) : BigDecimal.ZERO;
        BigDecimal insurance = nz(req.getCargoValue()).multiply(dec(config, "insuranceRate", new BigDecimal("0.003")));
        BigDecimal cod = nz(req.getCodAmount()).multiply(dec(config, "codRate", new BigDecimal("0.006")));
        BigDecimal remote = Boolean.TRUE.equals(req.getRemote()) ? dec(config, "remoteFee", new BigDecimal("20")) : BigDecimal.ZERO;
        BigDecimal holiday = Boolean.TRUE.equals(req.getHoliday()) ? dec(config, "holidayFee", BigDecimal.ZERO) : BigDecimal.ZERO;

        BigDecimal total = base.add(fuel).add(loading).add(upstairs).add(insurance).add(cod).add(remote).add(holiday);

        List<Map<String, Object>> detail = new ArrayList<>();
        detail.add(detailItem("基础运费", base, formula));
        if (fuel.signum() > 0) detail.add(detailItem("燃油附加", fuel, "基础运费 × " +
                dec(config, "fuelRate", new BigDecimal("0.10")).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString() + "%"));
        if (loading.signum() > 0) detail.add(detailItem("装卸费", loading, "增值服务：装卸"));
        if (upstairs.signum() > 0) detail.add(detailItem("上楼费", upstairs, "增值服务：上楼"));
        if (insurance.signum() > 0) detail.add(detailItem("保险费", insurance, "货值 " + req.getCargoValue().toPlainString() + " × " +
                dec(config, "insuranceRate", new BigDecimal("0.003")).multiply(new BigDecimal("1000")).stripTrailingZeros().toPlainString() + "‰"));
        if (cod.signum() > 0) detail.add(detailItem("代收手续费", cod, "代收金额 " + req.getCodAmount().toPlainString() + " × " +
                dec(config, "codRate", new BigDecimal("0.006")).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString() + "%"));
        if (remote.signum() > 0) detail.add(detailItem("偏远附加", remote, "偏远地区附加费"));
        if (holiday.signum() > 0) detail.add(detailItem("节假日附加", holiday, "节假日附加费"));
        detail.add(detailItem("合计", total, "以上各项合计"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("quoteNo", persistNumber ? numberService.next("BJ", tenantId) : "试算");
        result.put("baseFreight", money(base));
        result.put("fuelSurcharge", money(fuel));
        result.put("loadingFee", money(loading));
        result.put("upstairsFee", money(upstairs));
        result.put("insuranceFee", money(insurance));
        result.put("codFee", money(cod));
        result.put("remoteFee", money(remote));
        result.put("holidayFee", money(holiday));
        result.put("totalAmount", money(total));
        result.put("chargeWeight", chargeWeight.setScale(2, RoundingMode.HALF_UP));
        result.put("volumeWeight", volumeWeight.setScale(2, RoundingMode.HALF_UP));
        result.put("ruleVersion", rule == null ? "默认" : rule.getVersion());
        result.put("priceSource", source);
        result.put("mode", mode);
        result.put("detail", detail);
        result.put("validUntil", LocalDateTime.now().plusHours(48));
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("input", req);
        snapshot.put("config", config);
        snapshot.put("result", result);
        result.put("snapshot", snapshot);
        return result;
    }

    private PricingRule activeRule() {
        return ruleMapper.selectOne(new LambdaQueryWrapper<PricingRule>()
                .eq(PricingRule::getStatus, "已生效")
                .le(PricingRule::getEffectiveFrom, LocalDateTime.now())
                .orderByDesc(PricingRule::getId).last("limit 1"));
    }

    private Agreement matchAgreement(PriceRequest req) {
        if (req.getCustomerId() == null) return null;
        List<Agreement> list = agreementMapper.selectList(new LambdaQueryWrapper<Agreement>()
                .eq(Agreement::getCustomerId, req.getCustomerId())
                .eq(Agreement::getStatus, "生效中")
                .eq(Agreement::getOrigin, req.getOrigin())
                .eq(Agreement::getDestination, req.getDestination())
                .eq(Agreement::getVehicleType, req.getVehicleType()));
        LocalDateTime now = LocalDateTime.now();
        return list.stream()
                .filter(a -> a.getValidFrom() == null || !a.getValidFrom().isAfter(now))
                .filter(a -> a.getValidTo() == null || !a.getValidTo().isBefore(now))
                .findFirst().orElse(null);
    }

    @SuppressWarnings("unchecked")
    private BigDecimal matchRoute(Map<String, Object> config, PriceRequest req) {
        Object routes = config.get("routes");
        if (!(routes instanceof List<?> list)) return null;
        for (Object o : list) {
            if (o instanceof Map<?, ?> mm) {
                Map<String, Object> m = (Map<String, Object>) mm;
                String origin = String.valueOf(m.getOrDefault("origin", ""));
                String dest = String.valueOf(m.getOrDefault("destination", ""));
                String vehicle = String.valueOf(m.getOrDefault("vehicleType", req.getVehicleType()));
                if (origin.equals(req.getOrigin()) && dest.equals(req.getDestination()) && vehicle.equals(req.getVehicleType())) {
                    return new BigDecimal(String.valueOf(m.getOrDefault("price", "0")));
                }
            }
        }
        return null;
    }

    private BigDecimal distance(Map<String, Object> config, PriceRequest req) {
        Object dist = config.get("distances");
        if (dist instanceof Map<?, ?> m) {
            Object v = m.get(req.getOrigin() + "-" + req.getDestination());
            if (v != null) return new BigDecimal(String.valueOf(v));
        }
        return new BigDecimal("200");
    }

    public Map<String, Object> parse(String json) {
        if (json == null || json.isBlank()) return new LinkedHashMap<>();
        try {
            return objectMapper.readValue(json, new TypeReference<LinkedHashMap<String, Object>>() {
            });
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private Map<String, Object> detailItem(String name, BigDecimal amount, String formula) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name);
        m.put("amount", money(amount));
        m.put("formula", formula);
        return m;
    }

    public static BigDecimal money(BigDecimal v) {
        return nz(v).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static BigDecimal dec(Map<String, Object> config, String key, BigDecimal def) {
        Object v = config.get(key);
        if (v == null) return def;
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static String str(Map<String, Object> config, String key, String def) {
        Object v = config.get(key);
        return v == null ? def : String.valueOf(v);
    }
}
