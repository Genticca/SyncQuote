package com.syncquote.exchange.support;

public enum ExchangeType {
    UPBIT("UPBIT", "upbit"),
    BINANCE("BINANCE", "binance");

    private final String apiCode;
    private final String storageCode;

    ExchangeType(String apiCode, String storageCode) {
        this.apiCode = apiCode;
        this.storageCode = storageCode;
    }

    public String apiCode() {
        return apiCode;
    }

    public String storageCode() {
        return storageCode;
    }

    public static ExchangeType from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("exchange is required");
        }

        String normalized = value.trim();
        for (ExchangeType exchangeType : values()) {
            if (exchangeType.apiCode.equalsIgnoreCase(normalized)
                    || exchangeType.storageCode.equalsIgnoreCase(normalized)) {
                return exchangeType;
            }
        }
        throw new IllegalArgumentException("unsupported exchange: " + value);
    }
}
