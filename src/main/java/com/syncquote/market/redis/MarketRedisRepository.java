package com.syncquote.market.redis;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.syncquote.common.redis.RedisKeyFactory;
import com.syncquote.exchange.support.ExchangeType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class MarketRedisRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public MarketRedisRepository(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public List<MarketRedis> findByExchange(ExchangeType exchange) {
        String key = RedisKeyFactory.markets(exchange);
        return redisTemplate.opsForHash()
                .values(key)
                .stream()
                .map(Object::toString)
                .map(this::readMarket)
                .filter(Objects::nonNull)
                .toList();
    }

    public void saveAll(ExchangeType exchange, List<MarketRedis> markets) {
        String key = RedisKeyFactory.markets(exchange);
        redisTemplate.delete(key);
        if (markets.isEmpty()) {
            return;
        }
        markets.forEach(market -> redisTemplate.opsForHash().put(key, market.market(), writeMarket(market)));
    }

    private MarketRedis readMarket(String json) {
        try {
            return objectMapper.readValue(json, MarketRedis.class);
        } catch (JacksonException exception) {
            return null;
        }
    }

    private String writeMarket(MarketRedis market) {
        try {
            return objectMapper.writeValueAsString(market);
        } catch (JacksonException exception) {
            throw new IllegalStateException("failed to serialize market redis value", exception);
        }
    }
}
