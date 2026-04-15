package com.banking.accounts.infrastructure.controller;

import com.banking.accounts.application.dto.ActualizarCuentaRequest;
import com.banking.accounts.application.dto.CrearCuentaRequest;
import com.banking.accounts.application.dto.CuentaResponse;
import com.banking.accounts.application.usecase.ActualizarCuentaUseCase;
import com.banking.accounts.application.usecase.ConsultarCuentaUseCase;
import com.banking.accounts.application.usecase.CrearCuentaUseCase;
import com.banking.accounts.application.usecase.EliminarCuentaUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cuentas")
public class AccountController {

    private final CrearCuentaUseCase crearCuentaUseCase;
    private final ConsultarCuentaUseCase consultarCuentaUseCase;
    private final ActualizarCuentaUseCase actualizarCuentaUseCase;
    private final EliminarCuentaUseCase eliminarCuentaUseCase;

    public AccountController(CrearCuentaUseCase crearCuentaUseCase,
                             ConsultarCuentaUseCase consultarCuentaUseCase,
                             ActualizarCuentaUseCase actualizarCuentaUseCase,
                             EliminarCuentaUseCase eliminarCuentaUseCase) {
        this.crearCuentaUseCase = crearCuentaUseCase;
        this.consultarCuentaUseCase = consultarCuentaUseCase;
        this.actualizarCuentaUseCase = actualizarCuentaUseCase;
        this.eliminarCuentaUseCase = eliminarCuentaUseCase;
    }

    @PostMapping
    public ResponseEntity<CuentaResponse> crear(@RequestBody @Valid CrearCuentaRequest request) {
        return ResponseEntity.status(201).body(crearCuentaUseCase.ejecutar(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuentaResponse> consultar(@PathVariable Long id) {
        return ResponseEntity.ok(consultarCuentaUseCase.ejecutar(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CuentaResponse> actualizar(@PathVariable Long id,
                                                      @RequestBody ActualizarCuentaRequest request) {
        return ResponseEntity.ok(actualizarCuentaUseCase.ejecutar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CuentaResponse> eliminar(@PathVariable Long id) {
        return ResponseEntity.ok(eliminarCuentaUseCase.ejecutar(id));
    }
}
