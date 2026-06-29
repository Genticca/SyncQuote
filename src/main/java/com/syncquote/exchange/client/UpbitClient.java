package com.syncquote.exchange.client;

import com.syncquote.exchange.dto.UpbitMarketResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

@Component
public class UpbitClient {

    private final RestClient restClient;

    public UpbitClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.upbit.com")
                .build();
    }

    public List<UpbitMarketResponse> fetchMarkets() {
        UpbitMarketResponse[] response = restClient.get()
                .uri("/v1/market/all?isDetails=true")
                .retrieve()
                .body(UpbitMarketResponse[].class);
        return response == null ? List.of() : Arrays.asList(response);
    }
}
