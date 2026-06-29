package com.syncquote.ticker.redis;

import java.math.BigDecimal;

public class TickerRestMetaRedis {

    private BigDecimal highPrice52w;
    private BigDecimal lowPrice52w;

    public BigDecimal getHighPrice52w() {
        return highPrice52w;
    }

    public void setHighPrice52w(BigDecimal highPrice52w) {
        this.highPrice52w = highPrice52w;
    }

    public BigDecimal getLowPrice52w() {
        return lowPrice52w;
    }

    public void setLowPrice52w(BigDecimal lowPrice52w) {
        this.lowPrice52w = lowPrice52w;
    }
}
