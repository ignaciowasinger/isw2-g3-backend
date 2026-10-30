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
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;

/**
 * Entidad de línea de pedido.
 *
 * <p>Captura de forma inmutable el precio unitario vigente al registrar el pedido
 * (RN-06), de modo que cambios posteriores de precio no alteran comprobantes históricos.
 * La restricción única {@code (pedido_id, producto_id)} impide duplicar el mismo producto
 * dentro de un pedido.</p>
 */
@Entity
@Table(name = "item_pedido", uniqueConstraints = @UniqueConstraint(
        name = "uq_item_producto", columnNames = {"pedido_id", "producto_id"}))
public class ItemPedido {

    /** Identificador autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Pedido al que pertenece la línea. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    /** Producto solicitado. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    /** Unidades solicitadas. */
    @Column(nullable = false)
    private Integer cantidad;

    /** Precio unitario congelado al momento del registro. */
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    /** @return identificador de la línea. */
    public Integer getId() { return id; }

    /** @param id identificador de la línea. */
    public void setId(Integer id) { this.id = id; }

    /** @return pedido de la línea. */
    public Pedido getPedido() { return pedido; }

    /** @param pedido pedido de la línea. */
    public void setPedido(Pedido pedido) { this.pedido = pedido; }

    /** @return producto solicitado. */
    public Producto getProducto() { return producto; }

    /** @param producto producto solicitado. */
    public void setProducto(Producto producto) { this.producto = producto; }

    /** @return cantidad solicitada. */
    public Integer getCantidad() { return cantidad; }

    /** @param cantidad cantidad solicitada. */
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    /** @return precio unitario histórico. */
    public BigDecimal getPrecioUnitario() { return precioUnitario; }

    /** @param precioUnitario precio unitario histórico. */
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
}
