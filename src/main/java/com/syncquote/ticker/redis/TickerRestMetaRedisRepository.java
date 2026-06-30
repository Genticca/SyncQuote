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
        // 수집기(upbit.go)가 Hash에 쓰는 실제 필드명과 일치시켜야 함.
        // 기존 high_price_52w/low_price_52w 는 생산자에 없는 키라 항상 null이었음.
        meta.setHighPrice52w(parse(fields.get("highest_52_week_price")));
        meta.setLowPrice52w(parse(fields.get("lowest_52_week_price")));
        return meta;
    }

    private BigDecimal parse(String value) {
        return value == null ? null : new BigDecimal(value);
    }
}
