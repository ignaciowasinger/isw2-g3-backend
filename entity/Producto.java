package com.example.backendprojt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Entidad de producto de la carta.
 *
 * <p>Pertenece a un local y a una categoría. El stock se descuenta de forma transaccional
 * al crear pedidos y se repone al cancelarlos. La baja es lógica mediante {@link #activo}.</p>
 */
@Entity
@Table(name = "producto")
public class Producto {

    /** Identificador autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Local al que pertenece el producto. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "local_id", nullable = false)
    private Local local;

    /** Categoría de la carta. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    /** Nombre visible del producto. */
    @Column(nullable = false, length = 80)
    private String nombre;

    /** Descripción opcional. */
    @Column(length = 200)
    private String descripcion;

    /** Precio unitario vigente. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    /** Unidades disponibles. */
    @Column(nullable = false)
    private Integer stock;

    /** Bandera de baja lógica (RN-10). */
    @Column(nullable = false)
    private boolean activo = true;

    /** @return identificador del producto. */
    public Integer getId() { return id; }

    /** @param id identificador del producto. */
    public void setId(Integer id) { this.id = id; }

    /** @return local del producto. */
    public Local getLocal() { return local; }

    /** @param local local del producto. */
    public void setLocal(Local local) { this.local = local; }

    /** @return categoría del producto. */
    public Categoria getCategoria() { return categoria; }

    /** @param categoria categoría del producto. */
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    /** @return nombre del producto. */
    public String getNombre() { return nombre; }

    /** @param nombre nombre del producto. */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /** @return descripción del producto. */
    public String getDescripcion() { return descripcion; }

    /** @param descripcion descripción del producto. */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /** @return precio unitario. */
    public BigDecimal getPrecio() { return precio; }

    /** @param precio precio unitario. */
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    /** @return stock disponible. */
    public Integer getStock() { return stock; }

    /** @param stock stock disponible. */
    public void setStock(Integer stock) { this.stock = stock; }

    /** @return {@code true} si el producto está activo. */
    public boolean isActivo() { return activo; }

    /** @param activo estado lógico del producto. */
    public void setActivo(boolean activo) { this.activo = activo; }
}
