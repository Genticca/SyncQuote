package com.syncquote.ticker.controller;

import com.syncquote.global.response.ApiResponse;
import com.syncquote.ticker.dto.TickerRequestDto;
import com.syncquote.ticker.dto.TickerResponseDto;
import com.syncquote.ticker.service.TickerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/tickers")
public class TickerController {

    private final TickerService tickerService;

    public TickerController(TickerService tickerService) {
        this.tickerService = tickerService;
    }

    @GetMapping
    public ApiResponse<List<TickerResponseDto>> findTickers(@Valid TickerRequestDto request) {
        return ApiResponse.success(tickerService.findTickers(request));
    }

    @GetMapping("/{exchange}/{market}")
    public ApiResponse<TickerResponseDto> findTicker(
            @PathVariable @Pattern(regexp = "UPBIT|BINANCE", message = "exchange 값이 올바르지 않습니다.") String exchange,
            @PathVariable String market
    ) {
        return ApiResponse.success(tickerService.findTicker(exchange, market));
    }
}
