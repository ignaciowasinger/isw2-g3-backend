package com.example.backendprojt.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Detalle operativo de un pedido para el personal (RF-07).
 *
 * <p>A diferencia de {@link PedidoResponse}, incluye datos de contacto del cliente,
 * dirección, identificadores y repartidor asignado. Solo se expone a OPERADOR y ADMIN.</p>
 *
 * @param id                identificador interno del pedido.
 * @param codigoSeguimiento código público.
 * @param estado            estado actual.
 * @param localId           identificador del local.
 * @param localNombre       nombre del local.
 * @param clienteNombre     nombre del cliente.
 * @param clienteTelefono   teléfono del cliente.
 * @param direccionEntrega  dirección de entrega.
 * @param repartidorId      identificador del repartidor, o {@code null}.
 * @param repartidorNombre  nombre del repartidor, o {@code null}.
 * @param fechaHora         fecha y hora de registro.
 * @param items             líneas del pedido.
 * @param subtotal          suma de líneas.
 * @param descuento         descuento aplicado.
 * @param costoEnvio        costo de envío.
 * @param total             total a pagar.
 */
public record PedidoDetalleResponse(
        Integer id, String codigoSeguimiento, String estado, Integer localId, String localNombre,
        String clienteNombre, String clienteTelefono, String direccionEntrega, Integer repartidorId,
        String repartidorNombre, LocalDateTime fechaHora, List<PedidoResponse.ItemResponse> items,
        BigDecimal subtotal, BigDecimal descuento, BigDecimal costoEnvio, BigDecimal total) {
}
