package com.syncquote.candle.redis;

import com.syncquote.common.redis.RedisKeyFactory;
import com.syncquote.exchange.support.ExchangeType;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Repository
public class CandleRedisRepository {

    private static final String CANDLE_ONE_SECOND_PATTERN = "candles:1s:*:*";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public CandleRedisRepository(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    // REST 조회: to 이전(<=) 봉 중 최근 N개를 time 오름차순으로 반환.
    public List<CandleOneSecondRedis> findRecent(ExchangeType exchange, String market, long to, int limit) {
        List<CandleOneSecondRedis> candles = readAscending(RedisKeyFactory.candlesOneSecond(exchange, market)).stream()
                .filter(candle -> candle.time() <= to)
                .toList();
        if (candles.size() <= limit) {
            return candles;
        }
        return List.copyOf(candles.subList(candles.size() - limit, candles.size()));
    }

    // SSE 폴링: 활성화된 1초봉 List 키 전체.
    public List<String> findStreamKeys() {
        ScanOptions options = ScanOptions.scanOptions().match(CANDLE_ONE_SECOND_PATTERN).count(100).build();
        List<String> keys = new ArrayList<>();
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        }
        return keys;
    }

    // SSE 폴링: 해당 키의 1초봉 전체를 time 오름차순으로 반환.
    public List<CandleOneSecondRedis> findByKey(String key) {
        return readAscending(key);
    }

    private List<CandleOneSecondRedis> readAscending(String key) {
        List<String> values = redisTemplate.opsForList().range(key, 0, -1);
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .map(this::read)
                .filter(Objects::nonNull)
                .filter(candle -> candle.time() != null)
                .sorted(Comparator.comparingLong(CandleOneSecondRedis::time))
                .toList();
    }

    private CandleOneSecondRedis read(String json) {
        try {
            return objectMapper.readValue(json, CandleOneSecondRedis.class);
        } catch (JacksonException exception) {
            return null;
        }
    }
}
