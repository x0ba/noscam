package me.danielx.api.common;

public record ApiErrorResponse(
    String code, String message, boolean retryable, Integer retryAfterSeconds) {}
