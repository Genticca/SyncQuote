package com.syncquote.exchange.support;

public class MarketCodeNormalizer {

    private MarketCodeNormalizer() {
    }

    public static String toSymbol(String market) {
        if (market == null || market.isBlank()) {
            return "";
        }

        String trimmed = market.trim().toUpperCase();
        int dashIndex = trimmed.indexOf('-');
        if (dashIndex >= 0 && dashIndex < trimmed.length() - 1) {
            return trimmed.substring(dashIndex + 1);
        }

        if (trimmed.endsWith("USDT") && trimmed.length() > 4) {
            return trimmed.substring(0, trimmed.length() - 4);
        }
        if (trimmed.endsWith("BTC") && trimmed.length() > 3) {
            return trimmed.substring(0, trimmed.length() - 3);
        }
        if (trimmed.endsWith("ETH") && trimmed.length() > 3) {
            return trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed;
    }
}
