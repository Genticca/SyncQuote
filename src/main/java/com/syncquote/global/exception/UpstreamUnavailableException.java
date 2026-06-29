package com.syncquote.global.exception;

import org.springframework.http.HttpStatus;

public class UpstreamUnavailableException extends ApiException {

    public UpstreamUnavailableException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, "UPSTREAM_UNAVAILABLE", message);
    }
}
