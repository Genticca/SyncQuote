package com.syncquote.exchange.client;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.syncquote.exchange.dto.BinanceTickerResponse;
import com.syncquote.ticker.stream.TickerStreamConsumer;
import com.syncquote.ticker.stream.TickerStreamMessage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class BinanceClient {

    private static final Logger log = LoggerFactory.getLogger(BinanceClient.class);

    // TODO: MarketSyncService가 마켓 목록을 제공하기 전까지 임시로 고정한 구독 대상
    private static final List<String> SUBSCRIBE_SYMBOLS = List.of("BTCUSDT", "ETHUSDT");

    private final ObjectMapper objectMapper;
    private final TickerStreamConsumer tickerStreamConsumer;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor();

    private volatile WebSocket webSocket;

    public BinanceClient(ObjectMapper objectMapper, TickerStreamConsumer tickerStreamConsumer) {
        this.objectMapper = objectMapper;
        this.tickerStreamConsumer = tickerStreamConsumer;
    }

    @PostConstruct
    public void connect() {
        httpClient.newWebSocketBuilder()
                .buildAsync(buildEndpoint(), new BinanceListener())
                .thenAccept(ws -> this.webSocket = ws)
                .exceptionally(ex -> {
                    log.warn("Binance WebSocket 연결 실패, 재시도 예약", ex);
                    scheduleReconnect();
                    return null;
                });
    }

    @PreDestroy
    public void disconnect() {
        reconnectExecutor.shutdownNow();
        if (webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "shutdown");
        }
    }

    private void scheduleReconnect() {
        reconnectExecutor.schedule(this::connect, 3, TimeUnit.SECONDS);
    }

    private URI buildEndpoint() {
        String streams = SUBSCRIBE_SYMBOLS.stream()
                .map(symbol -> symbol.toLowerCase() + "@ticker")
                .collect(Collectors.joining("/"));
        return URI.create("wss://stream.binance.com:9443/stream?streams=" + streams);
    }

    private class BinanceListener implements WebSocket.Listener {

        private final StringBuilder buffer = new StringBuilder();

        @Override
        public void onOpen(WebSocket webSocket) {
            log.info("Binance WebSocket 연결됨");
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            buffer.append(data);
            if (last) {
                handleMessage(buffer.toString());
                buffer.setLength(0);
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            log.warn("Binance WebSocket 종료: {} {}", statusCode, reason);
            scheduleReconnect();
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            log.warn("Binance WebSocket 오류", error);
            scheduleReconnect();
        }

        private void handleMessage(String json) {
            try {
                JsonNode root = objectMapper.readTree(json);
                JsonNode dataNode = root.has("data") ? root.get("data") : root;
                BinanceTickerResponse response = objectMapper.treeToValue(dataNode, BinanceTickerResponse.class);
                tickerStreamConsumer.onMessage(TickerStreamMessage.fromBinance(response));
            } catch (Exception e) {
                log.warn("Binance 메시지 처리 실패: {}", json, e);
            }
        }
    }
}
