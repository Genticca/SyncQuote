package com.syncquote.exchange.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UpbitTickerResponse(
        String type,
        String code,
        @JsonProperty("trade_price") BigDecimal tradePrice,
        String change,
        @JsonProperty("change_rate") BigDecimal changeRate,
        @JsonProperty("signed_change_rate") BigDecimal signedChangeRate,
        @JsonProperty("signed_change_price") BigDecimal signedChangePrice,
        @JsonProperty("acc_trade_price_24h") BigDecimal accTradePrice24h,
        @JsonProperty("acc_trade_volume_24h") BigDecimal accTradeVolume24h,
        @JsonProperty("high_price") BigDecimal highPrice,
        @JsonProperty("low_price") BigDecimal lowPrice,
        @JsonProperty("trade_timestamp") Long tradeTimestamp
) {
}
