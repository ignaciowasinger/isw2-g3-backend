package com.example.backendprojt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad de repartidor.
 *
 * <p>Solo los repartidores activos pueden recibir pedidos y aparecen en la consulta de
 * repartidores en calle (RF-12). El límite de tres pedidos simultáneos se controla en el
 * servicio con bloqueo pesimista (RN-03).</p>
 */
@Entity
@Table(name = "repartidor")
public class Repartidor {

    /** Identificador autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Nombre completo del repartidor. */
    @Column(name = "nombre_completo", nullable = false, length = 100)
    private String nombreCompleto;

    /** Teléfono de contacto opcional. */
    @Column(length = 15)
    private String telefono;

    /** Bandera de disponibilidad lógica. */
    @Column(nullable = false)
    private boolean activo = true;

    /** @return identificador del repartidor. */
    public Integer getId() { return id; }

    /** @param id identificador del repartidor. */
    public void setId(Integer id) { this.id = id; }

    /** @return nombre completo. */
    public String getNombreCompleto() { return nombreCompleto; }

    /** @param nombreCompleto nombre completo. */
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    /** @return teléfono. */
    public String getTelefono() { return telefono; }

    /** @param telefono teléfono. */
    public void setTelefono(String telefono) { this.telefono = telefono; }

    /** @return {@code true} si el repartidor está activo. */
    public boolean isActivo() { return activo; }

    /** @param activo estado lógico del repartidor. */
    public void setActivo(boolean activo) { this.activo = activo; }
}
