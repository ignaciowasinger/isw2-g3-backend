package com.example.backendprojt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad de cliente final.
 *
 * <p>El teléfono es único y funciona como clave natural para reutilizar el cliente en
 * pedidos posteriores (RN-07). No se expone en la consulta pública de seguimiento.</p>
 */
@Entity
@Table(name = "cliente")
public class Cliente {

    /** Identificador autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Nombre del cliente. */
    @Column(nullable = false, length = 80)
    private String nombre;

    /** Teléfono único del cliente. */
    @Column(nullable = false, unique = true, length = 15)
    private String telefono;

    /** @return identificador del cliente. */
    public Integer getId() { return id; }

    /** @param id identificador del cliente. */
    public void setId(Integer id) { this.id = id; }

    /** @return nombre del cliente. */
    public String getNombre() { return nombre; }

    /** @param nombre nombre del cliente. */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /** @return teléfono del cliente. */
    public String getTelefono() { return telefono; }

    /** @param telefono teléfono del cliente. */
    public void setTelefono(String telefono) { this.telefono = telefono; }
}
