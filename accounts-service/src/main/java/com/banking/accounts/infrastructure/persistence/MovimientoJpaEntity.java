package com.banking.accounts.infrastructure.persistence;

import com.banking.accounts.domain.model.TipoMovimiento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimiento")
public class MovimientoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 12, nullable = false)
    private TipoMovimiento tipoMovimiento;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "saldo_resultante", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoResultante;

    @Column(name = "cuenta_id", nullable = false)
    private Long cuentaId;

    @Column(name = "movimiento_origen_id")
    private Long movimientoOrigenId;

    @Column(length = 500, nullable = true)
    private String justificacion;

    public MovimientoJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public TipoMovimiento getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(TipoMovimiento tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public BigDecimal getSaldoResultante() {
        return saldoResultante;
    }

    public void setSaldoResultante(BigDecimal saldoResultante) {
        this.saldoResultante = saldoResultante;
    }

    public Long getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(Long cuentaId) {
        this.cuentaId = cuentaId;
    }

    public Long getMovimientoOrigenId() {
        return movimientoOrigenId;
    }

    public void setMovimientoOrigenId(Long movimientoOrigenId) {
        this.movimientoOrigenId = movimientoOrigenId;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
    }
}
