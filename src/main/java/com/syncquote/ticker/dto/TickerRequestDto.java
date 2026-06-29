package com.syncquote.ticker.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import java.util.Arrays;
import java.util.List;

public class TickerRequestDto {

    @Pattern(regexp = "UPBIT|BINANCE", message = "exchange 값이 올바르지 않습니다.")
    private String exchange;

    private String markets;

    @Pattern(regexp = "ALL|TOP_GAINERS|TOP_VOLUME", message = "category 값이 올바르지 않습니다.")
    private String category = "ALL";

    @Min(value = 1, message = "limit은 1 이상이어야 합니다.")
    @Max(value = 500, message = "limit은 500 이하여야 합니다.")
    private Integer limit;

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getMarkets() {
        return markets;
    }

    public void setMarkets(String markets) {
        this.markets = markets;
    }

    public List<String> getMarketList() {
        if (markets == null || markets.isBlank()) {
            return List.of();
        }
        return Arrays.stream(markets.split(","))
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .toList();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
