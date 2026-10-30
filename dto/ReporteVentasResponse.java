package com.example.backendprojt.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Reporte completo de ventas por local (RF-11).
 *
 * @param desde         primer día del período.
 * @param hasta         último día del período.
 * @param locales       detalle por local.
 * @param totalPedidos  suma de pedidos entregados del período.
 * @param totalVendido  suma de {@code subtotal - descuento} del período.
 */
public record ReporteVentasResponse(LocalDate desde, LocalDate hasta,
                                    List<VentaLocalResponse> locales,
                                    long totalPedidos, BigDecimal totalVendido) {
}
