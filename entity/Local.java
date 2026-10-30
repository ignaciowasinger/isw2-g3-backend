package com.example.backendprojt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad de local gastronómico.
 *
 * <p>Cada local agrupa productos y pedidos. El nombre es único a nivel de sistema y la
 * baja se implementa con la bandera {@link #activo} (RN-10).</p>
 */
@Entity
@Table(name = "local")
public class Local {

    /** Identificador autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Nombre comercial único. */
    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    /** Dirección física opcional. */
    @Column(length = 120)
    private String direccion;

    /** Teléfono de contacto opcional. */
    @Column(length = 15)
    private String telefono;

    /** Bandera de baja lógica (RN-10). */
    @Column(nullable = false)
    private boolean activo = true;

    /** @return identificador del local. */
    public Integer getId() { return id; }

    /** @param id identificador del local. */
    public void setId(Integer id) { this.id = id; }

    /** @return nombre comercial. */
    public String getNombre() { return nombre; }

    /** @param nombre nombre comercial. */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /** @return dirección. */
    public String getDireccion() { return direccion; }

    /** @param direccion dirección. */
    public void setDireccion(String direccion) { this.direccion = direccion; }

    /** @return teléfono. */
    public String getTelefono() { return telefono; }

    /** @param telefono teléfono. */
    public void setTelefono(String telefono) { this.telefono = telefono; }

    /** @return {@code true} si el local está activo. */
    public boolean isActivo() { return activo; }

    /** @param activo estado lógico del local. */
    public void setActivo(boolean activo) { this.activo = activo; }
}
