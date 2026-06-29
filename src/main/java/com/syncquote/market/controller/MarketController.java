package com.syncquote.market.controller;

import com.syncquote.global.response.ApiResponse;
import com.syncquote.market.dto.MarketResponseDto;
import com.syncquote.market.service.MarketService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/markets")
public class MarketController {

    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    @GetMapping
    public ApiResponse<List<MarketResponseDto>> findMarkets(
            @RequestParam(value = "exchange", required = false) String exchange
    ) {
        return ApiResponse.success(marketService.findMarkets(exchange));
    }
}
