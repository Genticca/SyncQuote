package com.syncquote.candle.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;

public class CandleRequestDto {

    @NotBlank(message = "exchange는 필수입니다.")
    @Pattern(regexp = "UPBIT|BINANCE", message = "exchange 값이 올바르지 않습니다.")
    private String exchange;

    @NotBlank(message = "market은 필수입니다.")
    private String market;

    @NotBlank(message = "interval은 필수입니다.")
    private String interval;

    @Min(value = 1, message = "limit은 1 이상이어야 합니다.")
    @Max(value = 1000, message = "limit은 1000 이하여야 합니다.")
    private Integer limit;

    private Long to;

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

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    // 미지정 시 기본 200.
    public Integer getLimit() {
        return limit == null ? 200 : limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    // 미지정 시 현재 시각(epoch 초).
    public Long getTo() {
        return to == null ? Instant.now().getEpochSecond() : to;
    }

    public void setTo(Long to) {
        this.to = to;
    }
}
