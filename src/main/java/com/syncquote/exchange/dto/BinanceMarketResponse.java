package com.syncquote.exchange.dto;

public record BinanceMarketResponse(
        String symbol,
        String status,
        String baseAsset,
        String quoteAsset
) {

    public boolean isTradingUsdtPair() {
        return "TRADING".equalsIgnoreCase(status) && "USDT".equalsIgnoreCase(quoteAsset);
    }
}
