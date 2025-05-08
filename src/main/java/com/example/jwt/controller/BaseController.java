package com.example.jwt.controller;

import com.example.jwt.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class BaseController {

    protected <T> ResponseEntity<ApiResponse<T>> successResponse(T data) {
        return ResponseEntity.ok(
                ApiResponse.<T>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Success")
                        .data(data)
                        .build()
        );
    }

    protected <T> ResponseEntity<ApiResponse<T>> successResponse(String message, T data) {
        return ResponseEntity.ok(
                ApiResponse.<T>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message(message)
                        .data(data)
                        .build()
        );
    }

    protected <T> ResponseEntity<ApiResponse<T>> errorResponse(String message) {
        return ResponseEntity.badRequest().body(
                ApiResponse.<T>builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message(message)
                        .build()
        );
    }

    protected <T> ResponseEntity<ApiResponse<T>> errorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(
                ApiResponse.<T>builder()
                        .statusCode(status.value())
                        .message(message)
                        .build()
        );
    }

    protected <T> ResponseEntity<ApiResponse<T>> buildResponse(ApiResponse<T> apiResponse) {
        return ResponseEntity.status(apiResponse.getStatusCode()).body(apiResponse);
    }
}

