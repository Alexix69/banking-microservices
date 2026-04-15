package com.banking.accounts.infrastructure.controller;

import com.banking.accounts.application.dto.ReporteItemResponse;
import com.banking.accounts.application.usecase.GenerarReporteUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reportes")
public class ReporteController {

    private final GenerarReporteUseCase generarReporteUseCase;

    public ReporteController(GenerarReporteUseCase generarReporteUseCase) {
        this.generarReporteUseCase = generarReporteUseCase;
    }

    @GetMapping
    public ResponseEntity<List<ReporteItemResponse>> generar(
            @RequestParam(required = true) Long clienteId,
            @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(generarReporteUseCase.ejecutar(clienteId, fechaInicio, fechaFin));
    }
}
