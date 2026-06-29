package com.syncquote.common.redis;

public enum RedisKeyType {
    MARKETS("markets:%s"),
    CANDLES_ONE_SECOND("candles:1s:%s:%s"),
    TICKER_STREAM("ticker:%s:%s"),
    TICKER_REST_META("ticker:rest:%s:%s");

    private final String pattern;

    RedisKeyType(String pattern) {
        this.pattern = pattern;
    }

    public String format(Object... args) {
        return pattern.formatted(args);
    }
}
