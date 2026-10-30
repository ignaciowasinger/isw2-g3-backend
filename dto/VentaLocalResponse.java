package com.example.backendprojt.dto;

import java.math.BigDecimal;

/**
 * Ventas de un local en un período (RF-11).
 *
 * <p>{@code montoVendido} es {@code subtotal - descuento}; no incluye el costo de envío.</p>
 *
 * @param localId          identificador del local.
 * @param localNombre      nombre del local.
 * @param cantidadPedidos  cantidad de pedidos entregados en el período.
 * @param montoVendido     monto vendido en el período.
 */
public record VentaLocalResponse(Integer localId, String localNombre,
                                 long cantidadPedidos, BigDecimal montoVendido) {
}
