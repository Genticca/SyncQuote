package com.syncquote.exchange.client;

import com.syncquote.exchange.dto.BinanceMarketResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class BinanceClient {

    private final RestClient restClient;

    public BinanceClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.binance.com")
                .build();
    }

    public List<BinanceMarketResponse> fetchMarkets() {
        BinanceExchangeInfoResponse response = restClient.get()
                .uri("/api/v3/exchangeInfo")
                .retrieve()
                .body(BinanceExchangeInfoResponse.class);
        return response == null || response.symbols() == null ? List.of() : response.symbols();
    }

    public record BinanceExchangeInfoResponse(
            List<BinanceMarketResponse> symbols
    ) {
    }
}
