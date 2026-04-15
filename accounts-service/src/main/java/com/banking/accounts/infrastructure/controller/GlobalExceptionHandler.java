package com.banking.accounts.infrastructure.controller;

import com.banking.accounts.domain.exception.BusinessRuleException;
import com.banking.accounts.domain.exception.CuentaInactivaException;
import com.banking.accounts.domain.exception.CuentaNotFoundException;
import com.banking.accounts.domain.exception.ClienteInactivoException;
import com.banking.accounts.domain.exception.ClienteNotFoundException;
import com.banking.accounts.domain.exception.JustificacionRequeridaException;
import com.banking.accounts.domain.exception.LimiteDiarioExcedidoException;
import com.banking.accounts.domain.exception.MovimientoNotFoundException;
import com.banking.accounts.domain.exception.NumeroCuentaDuplicadoException;
import com.banking.accounts.domain.exception.SaldoInicialInvalidoException;
import com.banking.accounts.domain.exception.SaldoInsuficienteException;
import com.banking.accounts.domain.exception.SaldoMinimoInsuficienteException;
import com.banking.accounts.domain.exception.ValorMovimientoInvalidoException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> handleSaldoInsuficiente(SaldoInsuficienteException ex, HttpServletRequest req) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, "Saldo no disponible", req);
    }

    @ExceptionHandler(LimiteDiarioExcedidoException.class)
    public ResponseEntity<Map<String, Object>> handleLimiteDiario(LimiteDiarioExcedidoException ex, HttpServletRequest req) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, "Límite de retiro diario excedido", req);
    }

    @ExceptionHandler(CuentaInactivaException.class)
    public ResponseEntity<Map<String, Object>> handleCuentaInactiva(CuentaInactivaException ex, HttpServletRequest req) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), req);
    }

    @ExceptionHandler(ClienteInactivoException.class)
    public ResponseEntity<Map<String, Object>> handleClienteInactivo(ClienteInactivoException ex, HttpServletRequest req) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), req);
    }

    @ExceptionHandler(CuentaNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCuentaNotFound(CuentaNotFoundException ex, HttpServletRequest req) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(MovimientoNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleMovimientoNotFound(MovimientoNotFoundException ex, HttpServletRequest req) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(ClienteNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleClienteNotFound(ClienteNotFoundException ex, HttpServletRequest req) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(NumeroCuentaDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> handleNumeroCuentaDuplicado(NumeroCuentaDuplicadoException ex, HttpServletRequest req) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(JustificacionRequeridaException.class)
    public ResponseEntity<Map<String, Object>> handleJustificacionRequerida(JustificacionRequeridaException ex, HttpServletRequest req) {
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

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(ValorMovimientoInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handleValorMovimientoInvalido(ValorMovimientoInvalidoException ex, HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(SaldoMinimoInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> handleSaldoMinimo(SaldoMinimoInsuficienteException ex, HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(SaldoInicialInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handleSaldoInicialInvalido(SaldoInicialInvalidoException ex, HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessRule(BusinessRuleException ex, HttpServletRequest req) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception on {}: {}", req.getRequestURI(), ex.getMessage(), ex);
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
