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
        if (market == null || market.isBlank()) {
            return "";
        }

        String normalized = market.trim().toUpperCase();
        if (exchange == ExchangeType.UPBIT) {
            int dashIndex = normalized.indexOf('-');
            return dashIndex >= 0 ? normalized.substring(dashIndex + 1) : normalized;
        }
        return BINANCE_QUOTE_ASSETS.stream()
                .filter(quote -> normalized.length() > quote.length() && normalized.endsWith(quote))
                .findFirst()
                .map(quote -> normalized.substring(0, normalized.length() - quote.length()))
                .orElse(normalized);
    }

    public static String toSymbol(String market) {
        if (market == null || market.isBlank()) {
            return "";
        }
        String normalized = market.trim().toUpperCase();
        int dashIndex = normalized.indexOf('-');
        if (dashIndex >= 0 && dashIndex < normalized.length() - 1) {
            return normalized.substring(dashIndex + 1);
        }
        return BINANCE_QUOTE_ASSETS.stream()
                .filter(quote -> normalized.length() > quote.length() && normalized.endsWith(quote))
                .findFirst()
                .map(quote -> normalized.substring(0, normalized.length() - quote.length()))
                .orElse(normalized);
    }
}
