package com.syncquote.ticker.redis;

import com.syncquote.ticker.stream.TickerStreamMessage;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TickerStreamRedis {

    private TickerStreamRedis() {
    }

    public static Map<String, String> toFieldMap(TickerStreamMessage message) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("trade_price", message.tradePrice().toPlainString());
        fields.put("change_status", message.changeStatus());
        fields.put("change_rate", message.changeRate().toPlainString());
        fields.put("signed_change_rate", message.signedChangeRate().toPlainString());
        fields.put("signed_change_price", message.signedChangePrice().toPlainString());
        fields.put("acc_trade_price_24h", message.accTradePrice24h().toPlainString());
        fields.put("acc_trade_volume_24h", message.accTradeVolume24h().toPlainString());
        fields.put("high_price", message.highPrice().toPlainString());
        fields.put("low_price", message.lowPrice().toPlainString());
        fields.put("event_timestamp", String.valueOf(message.eventTimestamp()));
        return fields;
    }
}
