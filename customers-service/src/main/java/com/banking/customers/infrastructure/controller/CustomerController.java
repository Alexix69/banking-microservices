package com.banking.customers.infrastructure.controller;

import com.banking.customers.application.dto.ActualizarClienteRequest;
import com.banking.customers.application.dto.ClienteResponse;
import com.banking.customers.application.dto.CrearClienteRequest;
import com.banking.customers.application.usecase.ActualizarClienteUseCase;
import com.banking.customers.application.usecase.ConsultarClienteUseCase;
import com.banking.customers.application.usecase.CrearClienteUseCase;
import com.banking.customers.application.usecase.EliminarClienteUseCase;
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
@RequestMapping("/clientes")
public class CustomerController {

    private final CrearClienteUseCase crearClienteUseCase;
    private final ConsultarClienteUseCase consultarClienteUseCase;
    private final ActualizarClienteUseCase actualizarClienteUseCase;
    private final EliminarClienteUseCase eliminarClienteUseCase;

    public CustomerController(CrearClienteUseCase crearClienteUseCase,
                              ConsultarClienteUseCase consultarClienteUseCase,
                              ActualizarClienteUseCase actualizarClienteUseCase,
                              EliminarClienteUseCase eliminarClienteUseCase) {
        this.crearClienteUseCase = crearClienteUseCase;
        this.consultarClienteUseCase = consultarClienteUseCase;
        this.actualizarClienteUseCase = actualizarClienteUseCase;
        this.eliminarClienteUseCase = eliminarClienteUseCase;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@RequestBody @Valid CrearClienteRequest request) {
        return ResponseEntity.status(201).body(crearClienteUseCase.ejecutar(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> consultar(@PathVariable Long id) {
        return ResponseEntity.ok(consultarClienteUseCase.ejecutar(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> actualizar(@PathVariable Long id,
                                                       @RequestBody ActualizarClienteRequest request) {
        return ResponseEntity.ok(actualizarClienteUseCase.ejecutar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ClienteResponse> eliminar(@PathVariable Long id) {
        return ResponseEntity.ok(eliminarClienteUseCase.ejecutar(id));
    }
}
