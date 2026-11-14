package com.quantaida.wuliu.common.util;

import com.quantaida.wuliu.common.exception.BizException;

import java.util.List;
import java.util.Locale;

/** 禁运品双重校验：关键词库 + 类目勾选。 */
public final class DangerousGoodsChecker {
    private static final List<String> KEYWORDS = List.of(
            "炸药", "雷管", "导火索", "火药", "枪支", "弹药", "管制刀具", "毒品", "麻醉品",
            "硫酸", "盐酸", "硝酸", "汽油", "柴油", "酒精", "油漆", "香蕉水", "液化气", "氧气瓶",
            "活体动物", "野生动物", "象牙", "走私", "易燃", "易爆", "剧毒", "放射性"
    );

    private static final List<String> CATEGORIES = List.of("危险品", "易燃易爆", "活体动物", "枪支弹药", "走私货物");

    private DangerousGoodsChecker() {
    }

    public static void check(String cargoName, String cargoType) {
        String text = ((cargoName == null ? "" : cargoName) + " " + (cargoType == null ? "" : cargoType))
                .toLowerCase(Locale.ROOT);
        for (String keyword : KEYWORDS) {
            if (text.contains(keyword)) {
                throw new BizException("该货物属禁运品（命中「" + keyword + "」），无法下单");
            }
        }
        if (cargoType != null && CATEGORIES.contains(cargoType)) {
            throw new BizException("该货物类目属禁运范围，无法下单");
        }
    }
}
