package com.syncquote.global.response;

public record ApiError(
        String code,
        String message
) {
}
