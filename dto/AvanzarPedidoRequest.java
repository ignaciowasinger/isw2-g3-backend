package com.example.backendprojt.dto;

import jakarta.validation.constraints.Positive;

/**
 * Cuerpo opcional del endpoint que avanza el estado de un pedido (RN-05).
 *
 * <p>Solo se utiliza cuando la transición es {@code EN_PREPARACION -> EN_CAMINO}; en ese
 * caso es obligatorio indicar el repartidor que realizará la entrega. Para las demás
 * transiciones el cuerpo puede omitirse o enviarse vacío. Es un {@code record} inmutable.</p>
 *
 * @param repartidorId identificador del repartidor a asignar; debe ser positivo.
 */
public record AvanzarPedidoRequest(
        @Positive(message = "El repartidor debe ser un identificador válido")
        Integer repartidorId) {
}
