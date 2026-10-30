package com.example.backendprojt.entity;

/**
 * Estados del ciclo de vida de un pedido (RN-05).
 *
 * <p>El flujo operativo es {@code RECIBIDO -> EN_PREPARACION -> EN_CAMINO -> ENTREGADO};
 * {@code CANCELADO} es un estado terminal alternativo alcanzable solo desde
 * {@code RECIBIDO} o {@code EN_PREPARACION} (RN-04).</p>
 */
public enum EstadoPedido {
    /** Pedido registrado y pendiente de preparación. */
    RECIBIDO,
    /** Pedido en preparación en el local. */
    EN_PREPARACION,
    /** Pedido despachado con repartidor asignado. */
    EN_CAMINO,
    /** Pedido entregado al cliente. */
    ENTREGADO,
    /** Pedido cancelado con reposición de stock. */
    CANCELADO
}
