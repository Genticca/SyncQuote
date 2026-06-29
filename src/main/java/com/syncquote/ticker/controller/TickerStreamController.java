package com.syncquote.ticker.controller;

import com.syncquote.ticker.service.TickerStreamService;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Arrays;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/stream")
public class TickerStreamController {

    private final TickerStreamService tickerStreamService;

    public TickerStreamController(TickerStreamService tickerStreamService) {
        this.tickerStreamService = tickerStreamService;
    }

    @GetMapping(value = "/tickers", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTickers(
            @RequestParam(required = false) @Pattern(regexp = "UPBIT|BINANCE", message = "exchange 값이 올바르지 않습니다.") String exchange,
            @RequestParam(required = false) String markets
    ) {
        return tickerStreamService.subscribe(exchange, parseMarkets(markets));
    }

    private List<String> parseMarkets(String markets) {
        if (markets == null || markets.isBlank()) {
            return List.of();
        }
        return Arrays.stream(markets.split(","))
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .toList();
    }
}
