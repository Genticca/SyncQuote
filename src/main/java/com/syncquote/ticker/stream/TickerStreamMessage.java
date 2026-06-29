package com.syncquote.ticker.stream;

import java.math.BigDecimal;

public record TickerStreamMessage(
        String exchange,
        String market,
        BigDecimal tradePrice,
        String changeStatus,
        BigDecimal changeRate,
        BigDecimal signedChangeRate,
        BigDecimal signedChangePrice,
        BigDecimal accTradePrice24h,
        BigDecimal accTradeVolume24h,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        Long eventTimestamp
) {
}
