package com.syncquote.ticker.redis;

import com.syncquote.ticker.stream.TickerStreamMessage;

import java.math.BigDecimal;
import java.util.Map;

public final class TickerStreamRedis {

    private TickerStreamRedis() {
    }

    public static TickerStreamMessage fromFieldMap(String exchange, String market, Map<Object, Object> fields) {
        return new TickerStreamMessage(
                exchange,
                market,
                decimal(fields.get("trade_price")),
                string(fields.get("change_status")),
                decimal(fields.get("change_rate")),
                decimal(fields.get("signed_change_rate")),
                decimal(fields.get("signed_change_price")),
                decimal(fields.get("acc_trade_price_24h")),
                decimal(fields.get("acc_trade_volume_24h")),
                decimal(fields.get("high_price")),
                decimal(fields.get("low_price")),
                longValue(fields.get("event_timestamp"))
        );
    }

    private static String string(Object value) {
        return value == null ? null : value.toString();
    }

    private static BigDecimal decimal(Object value) {
        String stringValue = string(value);
        return stringValue == null || stringValue.isBlank() ? null : new BigDecimal(stringValue);
    }

    private static Long longValue(Object value) {
        String stringValue = string(value);
        return stringValue == null || stringValue.isBlank() ? null : Long.valueOf(stringValue);
    }
}
