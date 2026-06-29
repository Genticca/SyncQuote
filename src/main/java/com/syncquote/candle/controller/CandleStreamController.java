package com.syncquote.candle.controller;

import com.syncquote.candle.service.CandleStreamService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Validated
@RestController
@RequestMapping("/api/v1/stream")
public class CandleStreamController {

    private final CandleStreamService candleStreamService;

    public CandleStreamController(CandleStreamService candleStreamService) {
        this.candleStreamService = candleStreamService;
    }

    @GetMapping(value = "/candles", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCandles(
            @RequestParam(required = false) @NotBlank(message = "exchange는 필수입니다.")
            @Pattern(regexp = "UPBIT|BINANCE", message = "exchange 값이 올바르지 않습니다.") String exchange,
            @RequestParam(required = false) @NotBlank(message = "market은 필수입니다.") String market,
            @RequestParam(required = false) @NotBlank(message = "interval은 필수입니다.") String interval
    ) {
        return candleStreamService.subscribe(exchange, market);
    }
}
