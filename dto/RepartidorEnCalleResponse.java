package com.example.backendprojt.dto;

import java.util.List;

/**
 * Repartidor con sus pedidos en camino (RF-12).
 *
 * @param repartidorId     identificador del repartidor.
 * @param nombreCompleto   nombre completo.
 * @param cantidadEnCamino cantidad de pedidos actualmente en camino.
 * @param codigosPedidos   códigos de seguimiento de esos pedidos.
 */
public record RepartidorEnCalleResponse(Integer repartidorId, String nombreCompleto,
                                        int cantidadEnCamino, List<String> codigosPedidos) {
}
