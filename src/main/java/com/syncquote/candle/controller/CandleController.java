package com.syncquote.candle.controller;

import com.syncquote.candle.dto.CandleRequestDto;
import com.syncquote.candle.dto.CandleResponseDto;
import com.syncquote.candle.service.CandleService;
import com.syncquote.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/candles")
public class CandleController {

    private final CandleService candleService;

    public CandleController(CandleService candleService) {
        this.candleService = candleService;
    }

    @GetMapping
    public ApiResponse<CandleResponseDto> getCandles(@Valid CandleRequestDto request) {
        return ApiResponse.success(candleService.getCandles(
                request.getExchange(),
                request.getMarket(),
                request.getInterval(),
                request.getLimit(),
                request.getTo()
        ));
    }
}
