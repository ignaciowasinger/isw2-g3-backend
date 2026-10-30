package com.example.backendprojt.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Resumen de pedido para los listados operativos (RF-07).
 *
 * @param id               identificador del pedido.
 * @param codigoSeguimiento código público.
 * @param estado           estado actual.
 * @param localNombre      nombre del local.
 * @param clienteNombre    nombre del cliente.
 * @param repartidorNombre nombre del repartidor, o {@code null}.
 * @param fechaHora        fecha y hora de registro.
 * @param total            total del pedido.
 */
public record PedidoResumenResponse(
        Integer id, String codigoSeguimiento, String estado, String localNombre,
        String clienteNombre, String repartidorNombre, LocalDateTime fechaHora, BigDecimal total) {
}
