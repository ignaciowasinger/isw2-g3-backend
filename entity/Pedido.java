package com.example.backendprojt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad de pedido.
 *
 * <p>Guarda el comprobante financiero calculado por el backend, el estado del flujo, las
 * referencias a cliente, local y repartidor, y el código público de seguimiento. Los
 * importes se persisten con dos decimales y no se recalculan al consultar.</p>
 */
@Entity
@Table(name = "pedido")
public class Pedido {

    /** Identificador autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Código público único de 8 caracteres (RN-08). */
    @Column(name = "codigo_seguimiento", nullable = false, unique = true, length = 8)
    private String codigoSeguimiento;

    /** Cliente que recibe el pedido. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /** Local que prepara el pedido. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "local_id", nullable = false)
    private Local local;

    /** Repartidor asignado al despachar; nulo hasta pasar a en camino. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repartidor_id")
    private Repartidor repartidor;

    /** Dirección de entrega. */
    @Column(name = "direccion_entrega", nullable = false, length = 150)
    private String direccionEntrega;

    /** Fecha y hora de registro. */
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    /** Estado actual dentro del flujo operativo. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado;

    /** Suma de líneas antes de descuento y envío. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    /** Descuento aplicado (10 % si el subtotal supera $20.000,00). */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal descuento;

    /** Costo de envío fijo. */
    @Column(name = "costo_envio", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoEnvio;

    /** Total a pagar. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    /** @return identificador del pedido. */
    public Integer getId() { return id; }

    /** @param id identificador del pedido. */
    public void setId(Integer id) { this.id = id; }

    /** @return código de seguimiento. */
    public String getCodigoSeguimiento() { return codigoSeguimiento; }

    /** @param codigoSeguimiento código de seguimiento. */
    public void setCodigoSeguimiento(String codigoSeguimiento) { this.codigoSeguimiento = codigoSeguimiento; }

    /** @return cliente del pedido. */
    public Cliente getCliente() { return cliente; }

    /** @param cliente cliente del pedido. */
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    /** @return local del pedido. */
    public Local getLocal() { return local; }

    /** @param local local del pedido. */
    public void setLocal(Local local) { this.local = local; }

    /** @return repartidor asignado, o {@code null}. */
    public Repartidor getRepartidor() { return repartidor; }

    /** @param repartidor repartidor asignado. */
    public void setRepartidor(Repartidor repartidor) { this.repartidor = repartidor; }

    /** @return dirección de entrega. */
    public String getDireccionEntrega() { return direccionEntrega; }

    /** @param direccionEntrega dirección de entrega. */
    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }

    /** @return fecha y hora de registro. */
    public LocalDateTime getFechaHora() { return fechaHora; }

    /** @param fechaHora fecha y hora de registro. */
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    /** @return estado actual. */
    public EstadoPedido getEstado() { return estado; }

    /** @param estado estado actual. */
    public void setEstado(EstadoPedido estado) { this.estado = estado; }

    /** @return subtotal. */
    public BigDecimal getSubtotal() { return subtotal; }

    /** @param subtotal subtotal. */
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    /** @return descuento. */
    public BigDecimal getDescuento() { return descuento; }

    /** @param descuento descuento. */
    public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }

    /** @return costo de envío. */
    public BigDecimal getCostoEnvio() { return costoEnvio; }

    /** @param costoEnvio costo de envío. */
    public void setCostoEnvio(BigDecimal costoEnvio) { this.costoEnvio = costoEnvio; }

    /** @return total. */
    public BigDecimal getTotal() { return total; }

    /** @param total total. */
    public void setTotal(BigDecimal total) { this.total = total; }
}
