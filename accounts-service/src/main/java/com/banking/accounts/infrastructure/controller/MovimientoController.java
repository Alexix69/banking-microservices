package com.banking.accounts.infrastructure.controller;

import com.banking.accounts.application.dto.CrearAjusteRequest;
import com.banking.accounts.application.dto.CrearMovimientoRequest;
import com.banking.accounts.application.dto.CrearReversionRequest;
import com.banking.accounts.application.dto.MovimientoResponse;
import com.banking.accounts.application.usecase.ConsultarMovimientoUseCase;
import com.banking.accounts.application.usecase.RegistrarAjusteUseCase;
import com.banking.accounts.application.usecase.RegistrarMovimientoUseCase;
import com.banking.accounts.application.usecase.RegistrarReversionUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MovimientoController {

    private final RegistrarMovimientoUseCase registrarMovimientoUseCase;
    private final ConsultarMovimientoUseCase consultarMovimientoUseCase;
    private final RegistrarAjusteUseCase registrarAjusteUseCase;
    private final RegistrarReversionUseCase registrarReversionUseCase;

    public MovimientoController(RegistrarMovimientoUseCase registrarMovimientoUseCase,
                                ConsultarMovimientoUseCase consultarMovimientoUseCase,
                                RegistrarAjusteUseCase registrarAjusteUseCase,
                                RegistrarReversionUseCase registrarReversionUseCase) {
        this.registrarMovimientoUseCase = registrarMovimientoUseCase;
        this.consultarMovimientoUseCase = consultarMovimientoUseCase;
        this.registrarAjusteUseCase = registrarAjusteUseCase;
        this.registrarReversionUseCase = registrarReversionUseCase;
    }

    @PostMapping("/movimientos")
    public ResponseEntity<MovimientoResponse> registrar(@RequestBody @Valid CrearMovimientoRequest request) {
        return ResponseEntity.status(201).body(registrarMovimientoUseCase.ejecutar(request));
    }

    @GetMapping("/movimientos/{id}")
    public ResponseEntity<MovimientoResponse> consultar(@PathVariable Long id) {
        return ResponseEntity.ok(consultarMovimientoUseCase.ejecutar(id));
    }

    @PostMapping("/ajustes")
    public ResponseEntity<MovimientoResponse> ajuste(@RequestBody @Valid CrearAjusteRequest request) {
        return ResponseEntity.status(201).body(registrarAjusteUseCase.ejecutar(request));
    }

    @PostMapping("/reversiones")
    public ResponseEntity<MovimientoResponse> reversion(@RequestBody @Valid CrearReversionRequest request) {
        return ResponseEntity.status(201).body(registrarReversionUseCase.ejecutar(request));
    }
}
