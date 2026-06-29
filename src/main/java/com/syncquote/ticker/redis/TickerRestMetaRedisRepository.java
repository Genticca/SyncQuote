package com.syncquote.ticker.redis;

import com.syncquote.common.redis.RedisKeyFactory;
import com.syncquote.exchange.support.ExchangeType;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;

@Repository
public class TickerRestMetaRedisRepository {

    private final HashOperations<String, String, String> hashOperations;

    public TickerRestMetaRedisRepository(StringRedisTemplate redisTemplate) {
        this.hashOperations = redisTemplate.opsForHash();
    }

    public TickerRestMetaRedis find(ExchangeType exchange, String market) {
        String key = RedisKeyFactory.tickerRestMeta(exchange, market);
        Map<String, String> fields = hashOperations.entries(key);
        if (fields.isEmpty()) {
            return null;
        }
        TickerRestMetaRedis meta = new TickerRestMetaRedis();
        meta.setHighPrice52w(parse(fields.get("high_price_52w")));
        meta.setLowPrice52w(parse(fields.get("low_price_52w")));
        return meta;
    }

    private BigDecimal parse(String value) {
        return value == null ? null : new BigDecimal(value);
    }
}
