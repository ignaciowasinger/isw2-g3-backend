package com.example.backendprojt.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Comprobante público de un pedido (RF-06 y RN-08).
 *
 * <p>DTO inmutable que se devuelve tanto al crear un pedido como al consultarlo por
 * código. Deliberadamente no incluye teléfono, dirección, identificadores internos ni
 * datos del cliente.</p>
 *
 * @param codigoSeguimiento código público de 8 caracteres.
 * @param estado            estado actual del pedido.
 * @param localNombre       nombre del local.
 * @param fechaHora         fecha y hora de registro.
 * @param items             líneas del pedido.
 * @param subtotal          suma de líneas.
 * @param descuento         descuento aplicado.
 * @param costoEnvio        costo de envío fijo.
 * @param total             total a pagar.
 */
public record PedidoResponse(
        String codigoSeguimiento, String estado, String localNombre, LocalDateTime fechaHora,
        List<ItemResponse> items, BigDecimal subtotal, BigDecimal descuento,
        BigDecimal costoEnvio, BigDecimal total) {

    /**
     * Línea del comprobante público.
     *
     * @param productoNombre   nombre del producto.
     * @param cantidad         unidades.
     * @param precioUnitario   precio congelado al registrar el pedido.
     * @param subtotal         precio unitario por cantidad.
     */
    public record ItemResponse(String productoNombre, Integer cantidad,
                               BigDecimal precioUnitario, BigDecimal subtotal) {
    }
}
