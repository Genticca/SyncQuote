package com.syncquote.ticker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record TickerRestMetaResponseDto(
        String exchange,
        String market,
        @JsonProperty("high_price_52w") BigDecimal highPrice52w,
        @JsonProperty("low_price_52w") BigDecimal lowPrice52w
) {
}
