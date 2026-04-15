package com.banking.customers.infrastructure.controller;

import com.banking.customers.domain.exception.ClienteNotFoundException;
import com.banking.customers.domain.exception.ContrasenaInvalidaException;
import com.banking.customers.domain.exception.EdadInvalidaException;
import com.banking.customers.domain.exception.IdentificacionDuplicadaException;
import com.banking.customers.domain.exception.IdentificacionInvalidaException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EdadInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleEdadInvalida(EdadInvalidaException ex, HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(IdentificacionInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleIdentificacionInvalida(IdentificacionInvalidaException ex, HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(ContrasenaInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleContrasenaInvalida(ContrasenaInvalidaException ex, HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse("Solicitud inválida");
        return error(HttpStatus.BAD_REQUEST, message, req);
    }

    @ExceptionHandler(ClienteNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleClienteNotFound(ClienteNotFoundException ex, HttpServletRequest req) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(IdentificacionDuplicadaException.class)
    public ResponseEntity<Map<String, Object>> handleIdentificacionDuplicada(IdentificacionDuplicadaException ex, HttpServletRequest req) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex, HttpServletRequest req) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", req);
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message, HttpServletRequest req) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "path", req.getRequestURI()
        ));
    }
}
