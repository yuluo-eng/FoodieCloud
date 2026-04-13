package com.example.springbootblank.common.api;

public record ApiResponse<T>(int code, String msg, T data) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static <T> ApiResponse<T> ok(String msg, T data) {
        return new ApiResponse<>(200, msg, data);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(200, "success", null);
    }

    public static <T> ApiResponse<T> of(int code, String msg, T data) {
        return new ApiResponse<>(code, msg, data);
    }
}

