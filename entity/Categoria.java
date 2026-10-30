package com.example.backendprojt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad de categoría de la carta.
 *
 * <p>Dato maestro común a todos los locales; se carga con {@code data.sql} y se usa para
 * agrupar los productos en la carta pública (RF-05).</p>
 */
@Entity
@Table(name = "categoria")
public class Categoria {

    /** Identificador autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Nombre único de la categoría. */
    @Column(nullable = false, unique = true, length = 40)
    private String nombre;

    /** @return identificador de la categoría. */
    public Integer getId() { return id; }

    /** @param id identificador de la categoría. */
    public void setId(Integer id) { this.id = id; }

    /** @return nombre de la categoría. */
    public String getNombre() { return nombre; }

    /** @param nombre nombre de la categoría. */
    public void setNombre(String nombre) { this.nombre = nombre; }
}
