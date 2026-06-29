package com.syncquote.global.exception;

import org.springframework.http.HttpStatus;

public class TickerNotFoundException extends ApiException {

    public TickerNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "TICKER_NOT_FOUND", message);
    }
}
