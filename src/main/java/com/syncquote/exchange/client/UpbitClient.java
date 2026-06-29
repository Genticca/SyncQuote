package com.syncquote.exchange.client;

import tools.jackson.databind.ObjectMapper;
import com.syncquote.exchange.dto.UpbitTickerResponse;
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
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class UpbitClient {

    private static final Logger log = LoggerFactory.getLogger(UpbitClient.class);
    private static final URI ENDPOINT = URI.create("wss://api.upbit.com/websocket/v1");

    // TODO: MarketSyncService가 마켓 목록을 제공하기 전까지 임시로 고정한 구독 대상
    private static final List<String> SUBSCRIBE_MARKETS = List.of("KRW-BTC", "KRW-ETH");

    private final ObjectMapper objectMapper;
    private final TickerStreamConsumer tickerStreamConsumer;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor();

    private volatile WebSocket webSocket;

    public UpbitClient(ObjectMapper objectMapper, TickerStreamConsumer tickerStreamConsumer) {
        this.objectMapper = objectMapper;
        this.tickerStreamConsumer = tickerStreamConsumer;
    }

    @PostConstruct
    public void connect() {
        httpClient.newWebSocketBuilder()
                .buildAsync(ENDPOINT, new UpbitListener())
                .thenAccept(ws -> {
                    this.webSocket = ws;
                    ws.sendText(buildSubscribeMessage(), true);
                })
                .exceptionally(ex -> {
                    log.warn("Upbit WebSocket 연결 실패, 재시도 예약", ex);
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

    private String buildSubscribeMessage() {
        try {
            return objectMapper.writeValueAsString(List.of(
                    Map.of("ticket", "syncquote-" + System.currentTimeMillis()),
                    Map.of("type", "ticker", "codes", SUBSCRIBE_MARKETS),
                    Map.of("format", "DEFAULT")
            ));
        } catch (Exception e) {
            throw new IllegalStateException("Upbit 구독 메시지 생성 실패", e);
        }
    }

    private class UpbitListener implements WebSocket.Listener {

        private final StringBuilder buffer = new StringBuilder();

        @Override
        public void onOpen(WebSocket webSocket) {
            log.info("Upbit WebSocket 연결됨");
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            buffer.append(StandardCharsets.UTF_8.decode(data));
            if (last) {
                handleMessage(buffer.toString());
                buffer.setLength(0);
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            log.warn("Upbit WebSocket 종료: {} {}", statusCode, reason);
            scheduleReconnect();
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            log.warn("Upbit WebSocket 오류", error);
            scheduleReconnect();
        }

        private void handleMessage(String json) {
            try {
                UpbitTickerResponse response = objectMapper.readValue(json, UpbitTickerResponse.class);
                if (!"ticker".equals(response.type())) {
                    return;
                }
                tickerStreamConsumer.onMessage(TickerStreamMessage.fromUpbit(response));
            } catch (Exception e) {
                log.warn("Upbit 메시지 처리 실패: {}", json, e);
            }
        }
    }
}
