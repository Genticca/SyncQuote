package com.syncquote;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan({
        "com.syncquote.market.mapper",
        "com.syncquote.candle.mapper",
        "com.syncquote.ticker.mapper"
})
@SpringBootApplication
public class SyncquoteApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SyncquoteApiApplication.class, args);
    }

}
