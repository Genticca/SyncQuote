package com.syncquote.global.response;

public record ApiResponse<T>(boolean success, T data, ApiError error, long timestamp) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, System.currentTimeMillis());
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, new ApiError(code, message), System.currentTimeMillis());
    }

    public record ApiError(String code, String message) {
    }
}
