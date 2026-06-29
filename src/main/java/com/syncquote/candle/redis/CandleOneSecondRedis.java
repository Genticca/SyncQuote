package com.syncquote.candle.redis;

import java.math.BigDecimal;

// Redis 1초봉 List(candles:1s:{exchange}:{code})의 JSON 값 매핑.
// JSON 키(exchange, code, time, open, high, low, close, volume)가 레코드 컴포넌트명과 일치한다.
public record CandleOneSecondRedis(
        String exchange,
        String code,
        Long time,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        BigDecimal volume
) {
}
