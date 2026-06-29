package com.syncquote.common.redis;

public enum RedisKeyType {
    MARKETS("markets:%s"),
    CANDLE_1S("candles:1s:%s:%s"),
    TICKER_STREAM("ticker:%s:%s"),
    TICKER_REST_META("ticker:rest:%s:%s");

    private final String pattern;

    RedisKeyType(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}
