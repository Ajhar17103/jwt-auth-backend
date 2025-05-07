package com.example.jwt.exception;

import com.example.jwt.dto.Response;
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
        Response resp = new Response();
        resp.setStatusCode(HttpStatus.BAD_REQUEST.value());
        StringBuilder sb = new StringBuilder();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                sb.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ")
        );

        resp.setMessage(sb.toString().trim());
        return ResponseEntity.badRequest().body(resp);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Response> handleEntityNotFound(EntityNotFoundException ex) {
        Response resp = new Response();
        resp.setStatusCode(HttpStatus.NOT_FOUND.value());
        resp.setMessage(ex.getMessage() != null ? ex.getMessage() : "Requested entity not found.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Response> handleAccessDenied(AccessDeniedException ex) {
        Response resp = new Response();
        resp.setStatusCode(HttpStatus.FORBIDDEN.value());
        resp.setMessage("Access is denied.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> handleGenericException(Exception ex) {
        Response resp = new Response();
        resp.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        resp.setMessage("Internal Server Error: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
    }
}

