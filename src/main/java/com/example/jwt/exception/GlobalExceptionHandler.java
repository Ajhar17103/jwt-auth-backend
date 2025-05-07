package com.example.jwt.exception;

import com.example.jwt.dto.Response;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response> handleValidationErrors(MethodArgumentNotValidException ex) {
        StringBuilder sb = new StringBuilder();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                sb.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ")
        );

        Response resp = Response.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(sb.toString().trim())
                .build();

        return ResponseEntity.badRequest().body(resp);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Response> handleEntityNotFound(EntityNotFoundException ex) {
        Response resp = Response.builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage() != null ? ex.getMessage() : "Requested entity not found.")
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Response> handleAccessDenied(AccessDeniedException ex) {
        // Optional logging
        System.out.println("Access denied: " + ex.getMessage());

        Response resp = Response.builder()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .message(ex.getMessage() != null && !ex.getMessage().isBlank()
                        ? ex.getMessage()
                        : "You do not have permission to access this resource.")
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Response> handleExpiredJwt(ExpiredJwtException ex) {
        Response resp = Response.builder()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .message("JWT token has expired. Please log in again.")
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> handleGenericException(Exception ex) {
        // Optional logging
        System.err.println("Unhandled exception: " + ex.getMessage());

        Response resp = Response.builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Internal Server Error: " + ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
    }
}
