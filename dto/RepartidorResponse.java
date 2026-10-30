package com.example.backendprojt.dto;

/**
 * Repartidor expuesto por la API (RF-08).
 *
 * @param id             identificador del repartidor.
 * @param nombreCompleto nombre completo.
 * @param telefono       teléfono opcional.
 * @param activo         estado lógico.
 */
public record RepartidorResponse(Integer id, String nombreCompleto, String telefono, boolean activo) {
}
