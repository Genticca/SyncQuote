package com.syncquote.ticker.redis;

import com.syncquote.exchange.support.ExchangeType;
import com.syncquote.ticker.stream.TickerStreamMessage;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class TickerStreamRedisRepository {

    private static final String TICKER_STREAM_PATTERN = "ticker:*:*";

    private final StringRedisTemplate redisTemplate;

    public TickerStreamRedisRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public List<String> findStreamKeys() {
        ScanOptions options = ScanOptions.scanOptions().match(TICKER_STREAM_PATTERN).count(100).build();
        List<String> keys = new ArrayList<>();
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        }
        return keys;
    }

    @SuppressWarnings("unchecked")
    public List<StreamEntry> readAfter(String key, String lastRecordId) {
        List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream().read(
                StreamOffset.create(key, ReadOffset.from(lastRecordId))
        );
        if (records == null || records.isEmpty()) {
            return List.of();
        }

        KeyParts keyParts = parseKey(key);
        if (keyParts == null) {
            return List.of();
        }

        return records.stream()
                .map(record -> new StreamEntry(
                        key,
                        record.getId().getValue(),
                        TickerStreamRedis.fromFieldMap(keyParts.exchange(), keyParts.market(), record.getValue())
                ))
                .toList();
    }

    private KeyParts parseKey(String key) {
        String[] parts = key.split(":", 3);
        if (parts.length != 3 || !"ticker".equals(parts[0])) {
            return null;
        }
        return new KeyParts(ExchangeType.from(parts[1]).apiCode(), parts[2]);
    }

    public record StreamEntry(String key, String recordId, TickerStreamMessage message) {
    }

    private record KeyParts(String exchange, String market) {
    }
}
