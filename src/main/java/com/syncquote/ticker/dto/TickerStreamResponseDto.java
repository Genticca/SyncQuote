package com.syncquote.ticker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record TickerStreamResponseDto(
        String exchange,
        String market,
        @JsonProperty("trade_price") BigDecimal tradePrice,
        @JsonProperty("change_status") String changeStatus,
        @JsonProperty("change_rate") BigDecimal changeRate,
        @JsonProperty("signed_change_rate") BigDecimal signedChangeRate,
        @JsonProperty("signed_change_price") BigDecimal signedChangePrice,
        @JsonProperty("acc_trade_price_24h") BigDecimal accTradePrice24h,
        @JsonProperty("acc_trade_volume_24h") BigDecimal accTradeVolume24h,
        @JsonProperty("high_price") BigDecimal highPrice,
        @JsonProperty("low_price") BigDecimal lowPrice,
        @JsonProperty("event_timestamp") Long eventTimestamp
) {
}
