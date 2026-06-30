package com.syncquote.ticker.redis;

import com.syncquote.exchange.support.ExchangeType;
import com.syncquote.ticker.stream.TickerStreamMessage;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
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
    // "ticker:rest:{exch}:{code}" 는 52주 메타 Hash(스트림 아님)인데 glob "ticker:*:*" 의
    // '*' 가 ':' 도 매칭해 함께 잡힌다. 이 키에 opsForStream().read() 를 호출하면 WRONGTYPE
    // 예외가 나고, 그 폴 사이클 for 루프 전체가 중단돼 SSE가 산발적으로만 도달한다. → 제외.
    private static final String REST_TICKER_PREFIX = "ticker:rest:";

    private final StringRedisTemplate redisTemplate;

    public TickerStreamRedisRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public List<String> findStreamKeys() {
        ScanOptions options = ScanOptions.scanOptions().match(TICKER_STREAM_PATTERN).count(100).build();
        List<String> keys = new ArrayList<>();
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                String key = cursor.next();
                if (key.startsWith(REST_TICKER_PREFIX)) {
                    continue; // 메타 Hash → 스트림 폴링 대상 아님 (WRONGTYPE 방지)
                }
                keys.add(key);
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

    /** 스트림의 현재 마지막 레코드 ID. 신규 구독 시작점으로 써서 과거 backlog 재생을 건너뛴다. */
    public String latestId(String key) {
        List<MapRecord<String, Object, Object>> last = redisTemplate.opsForStream()
                .reverseRange(key, Range.<String>unbounded(), Limit.limit().count(1));
        if (last == null || last.isEmpty()) {
            return "0-0";
        }
        return last.get(0).getId().getValue();
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
