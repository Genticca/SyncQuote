package com.syncquote.exchange.support;

import java.util.Comparator;
import java.util.List;

public final class MarketCodeNormalizer {

    private static final List<String> BINANCE_QUOTE_ASSETS = List.of(
            "USDT", "FDUSD", "BUSD", "USDC", "TUSD", "BTC", "ETH", "BNB", "TRY", "EUR", "GBP", "KRW"
    ).stream().sorted(Comparator.comparingInt(String::length).reversed()).toList();

    private MarketCodeNormalizer() {
    }

    public static String toSymbol(ExchangeType exchange, String market) {
        if (exchange == ExchangeType.UPBIT) {
            int dashIndex = market.indexOf('-');
            return dashIndex >= 0 ? market.substring(dashIndex + 1) : market;
        }
        return BINANCE_QUOTE_ASSETS.stream()
                .filter(quote -> market.length() > quote.length() && market.endsWith(quote))
                .findFirst()
                .map(quote -> market.substring(0, market.length() - quote.length()))
                .orElse(market);
    }
}
