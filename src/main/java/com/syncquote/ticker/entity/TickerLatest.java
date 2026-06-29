package com.syncquote.ticker.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TickerLatest {

    private String exchange;
    private String market;
    private BigDecimal tradePrice;
    private String changeStatus;
    private BigDecimal changeRate;
    private BigDecimal signedChangeRate;
    private BigDecimal signedChangePrice;
    private BigDecimal accTradePrice24h;
    private BigDecimal accTradeVolume24h;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private Long eventTimestamp;
    private LocalDateTime updatedAt;

    // market_info 조인 결과로만 채워짐(ticker_latest 테이블 자체 컬럼 아님)
    private String koreanName;
    private String englishName;
    private String marketWarning;

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public BigDecimal getTradePrice() {
        return tradePrice;
    }

    public void setTradePrice(BigDecimal tradePrice) {
        this.tradePrice = tradePrice;
    }

    public String getChangeStatus() {
        return changeStatus;
    }

    public void setChangeStatus(String changeStatus) {
        this.changeStatus = changeStatus;
    }

    public BigDecimal getChangeRate() {
        return changeRate;
    }

    public void setChangeRate(BigDecimal changeRate) {
        this.changeRate = changeRate;
    }

    public BigDecimal getSignedChangeRate() {
        return signedChangeRate;
    }

    public void setSignedChangeRate(BigDecimal signedChangeRate) {
        this.signedChangeRate = signedChangeRate;
    }

    public BigDecimal getSignedChangePrice() {
        return signedChangePrice;
    }

    public void setSignedChangePrice(BigDecimal signedChangePrice) {
        this.signedChangePrice = signedChangePrice;
    }

    public BigDecimal getAccTradePrice24h() {
        return accTradePrice24h;
    }

    public void setAccTradePrice24h(BigDecimal accTradePrice24h) {
        this.accTradePrice24h = accTradePrice24h;
    }

    public BigDecimal getAccTradeVolume24h() {
        return accTradeVolume24h;
    }

    public void setAccTradeVolume24h(BigDecimal accTradeVolume24h) {
        this.accTradeVolume24h = accTradeVolume24h;
    }

    public BigDecimal getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(BigDecimal highPrice) {
        this.highPrice = highPrice;
    }

    public BigDecimal getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(BigDecimal lowPrice) {
        this.lowPrice = lowPrice;
    }

    public Long getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(Long eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getKoreanName() {
        return koreanName;
    }

    public void setKoreanName(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public String getMarketWarning() {
        return marketWarning;
    }

    public void setMarketWarning(String marketWarning) {
        this.marketWarning = marketWarning;
    }
}
