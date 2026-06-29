package com.syncquote.ticker.redis;

import com.syncquote.common.redis.RedisKeyFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;

@Repository
public class TickerRestMetaRedisRepository {

    private final HashOperations<String, String, String> hashOperations;
    private final RedisKeyFactory redisKeyFactory;

    public TickerRestMetaRedisRepository(StringRedisTemplate redisTemplate, RedisKeyFactory redisKeyFactory) {
        this.hashOperations = redisTemplate.opsForHash();
        this.redisKeyFactory = redisKeyFactory;
    }

    public TickerRestMetaRedis find(String exchange, String market) {
        String key = redisKeyFactory.tickerRestMetaKey(exchange, market);
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
