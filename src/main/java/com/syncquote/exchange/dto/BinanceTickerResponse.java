package com.syncquote.exchange.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BinanceTickerResponse(
        @JsonProperty("s") String symbol,
        @JsonProperty("c") BigDecimal lastPrice,
        @JsonProperty("p") BigDecimal priceChange,
        @JsonProperty("P") BigDecimal priceChangePercent,
        @JsonProperty("h") BigDecimal highPrice,
        @JsonProperty("l") BigDecimal lowPrice,
        @JsonProperty("v") BigDecimal baseVolume,
        @JsonProperty("q") BigDecimal quoteVolume,
        @JsonProperty("E") Long eventTime
) {
}
