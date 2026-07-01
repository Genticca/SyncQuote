package com.syncquote.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", "요청 파라미터가 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "지원하지 않는 요청 메서드입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "요청하신 리소스를 찾을 수 없습니다."),
    MARKET_NOT_FOUND(HttpStatus.NOT_FOUND, "MARKET_NOT_FOUND", "해당 마켓을 찾을 수 없습니다."),
    TICKER_NOT_FOUND(HttpStatus.NOT_FOUND, "TICKER_NOT_FOUND", "해당 현재가를 찾을 수 없습니다."),
    UPSTREAM_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "UPSTREAM_UNAVAILABLE", "외부 데이터 소스를 사용할 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}
