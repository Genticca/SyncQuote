package com.syncquote.global.exception;

import org.springframework.http.HttpStatus;

public class MarketNotFoundException extends ApiException {

    public MarketNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "MARKET_NOT_FOUND", message);
    }
}
