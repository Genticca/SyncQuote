package com.syncquote.common.redis;

import com.syncquote.exchange.support.ExchangeType;

public class RedisKeyFactory {

    private RedisKeyFactory() {
    }

    public static String markets(ExchangeType exchange) {
        return RedisKeyType.MARKETS.format(exchange.storageCode());
    }

    public static String candlesOneSecond(ExchangeType exchange, String market) {
        return RedisKeyType.CANDLES_ONE_SECOND.format(exchange.storageCode(), market);
    }

    public static String tickerStream(ExchangeType exchange, String market) {
        return RedisKeyType.TICKER_STREAM.format(exchange.storageCode(), market);
    }

    public static String tickerRestMeta(ExchangeType exchange, String market) {
        return RedisKeyType.TICKER_REST_META.format(exchange.storageCode(), market);
    }
}
