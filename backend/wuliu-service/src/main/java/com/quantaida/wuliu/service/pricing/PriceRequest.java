package com.quantaida.wuliu.service.pricing;

import lombok.Data;

import java.math.BigDecimal;

/** 询价入参（官网/小程序/后台共用）。 */
@Data
public class PriceRequest {
    private Long customerId;
    private String origin;
    private String destination;
    private String cargoType = "普货";
    private BigDecimal weight = BigDecimal.ZERO;
    private BigDecimal volume = BigDecimal.ZERO;
    private Integer pieces = 1;
    private String vehicleType = "6.8 米厢式";
    private BigDecimal cargoValue = BigDecimal.ZERO;
    private BigDecimal codAmount = BigDecimal.ZERO;
    private Boolean needLoading = false;
    private Boolean needUpstairs = false;
    private Boolean remote = false;
    private Boolean holiday = false;
    private String cargoName;
}
