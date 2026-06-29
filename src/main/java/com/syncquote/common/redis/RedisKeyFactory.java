package com.syncquote.common.redis;

import org.springframework.stereotype.Component;

@Component
public class RedisKeyFactory {

    public String marketsKey(String exchange) {
        return RedisKeyType.MARKETS.getPattern().formatted(lower(exchange));
    }

    public String candle1sKey(String exchange, String code) {
        return RedisKeyType.CANDLE_1S.getPattern().formatted(lower(exchange), code);
    }

    public String tickerStreamKey(String exchange, String code) {
        return RedisKeyType.TICKER_STREAM.getPattern().formatted(lower(exchange), code);
    }

    public String tickerRestMetaKey(String exchange, String code) {
        return RedisKeyType.TICKER_REST_META.getPattern().formatted(lower(exchange), code);
    }

    private String lower(String exchange) {
        return exchange.toLowerCase();
    }
}
