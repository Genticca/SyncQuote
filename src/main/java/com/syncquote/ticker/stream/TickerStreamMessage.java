package com.syncquote.ticker.stream;

import com.syncquote.exchange.dto.BinanceTickerResponse;
import com.syncquote.exchange.dto.UpbitTickerResponse;

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

    private static final BigDecimal PERCENT = BigDecimal.valueOf(100);

    // change_rate/signed_change_rate는 내부적으로 항상 소수(비율)로 들고 있는다.
    // 퍼센트(예 2.34) 변환은 API 응답을 만드는 곳(TickerService/TickerStreamConsumer)에서만 한다.
    public static TickerStreamMessage fromUpbit(UpbitTickerResponse r) {
        return new TickerStreamMessage(
                "UPBIT",
                r.code(),
                r.tradePrice(),
                r.change(),
                r.changeRate(),
                r.signedChangeRate(),
                r.signedChangePrice(),
                r.accTradePrice24h(),
                r.accTradeVolume24h(),
                r.highPrice(),
                r.lowPrice(),
                r.tradeTimestamp()
        );
    }

    public static TickerStreamMessage fromBinance(BinanceTickerResponse r) {
        BigDecimal signedChangePrice = r.priceChange();
        BigDecimal signedChangeRate = r.priceChangePercent().divide(PERCENT);
        String changeStatus = signedChangePrice.signum() > 0 ? "RISE" : signedChangePrice.signum() < 0 ? "FALL" : "EVEN";

        return new TickerStreamMessage(
                "BINANCE",
                r.symbol(),
                r.lastPrice(),
                changeStatus,
                signedChangeRate.abs(),
                signedChangeRate,
                signedChangePrice,
                r.quoteVolume(),
                r.baseVolume(),
                r.highPrice(),
                r.lowPrice(),
                r.eventTime()
        );
    }
}
