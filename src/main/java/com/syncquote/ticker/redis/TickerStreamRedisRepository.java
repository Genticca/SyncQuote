package com.syncquote.ticker.redis;

import com.syncquote.common.redis.RedisKeyFactory;
import com.syncquote.ticker.stream.TickerStreamMessage;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class TickerStreamRedisRepository {

    private static final long MAX_LEN = 120;
    private static final Duration TTL = Duration.ofSeconds(90);

    private final StringRedisTemplate redisTemplate;
    private final RedisKeyFactory redisKeyFactory;

    public TickerStreamRedisRepository(StringRedisTemplate redisTemplate, RedisKeyFactory redisKeyFactory) {
        this.redisTemplate = redisTemplate;
        this.redisKeyFactory = redisKeyFactory;
    }

    public void append(TickerStreamMessage message) {
        String key = redisKeyFactory.tickerStreamKey(message.exchange(), message.market());
        redisTemplate.opsForStream().add(MapRecord.create(key, TickerStreamRedis.toFieldMap(message)));
        redisTemplate.opsForStream().trim(key, MAX_LEN);
        redisTemplate.expire(key, TTL);
    }
}
