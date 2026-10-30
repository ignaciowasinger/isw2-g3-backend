package com.example.backendprojt.dto;

/**
 * Local expuesto por la API (RF-03).
 *
 * @param id        identificador del local.
 * @param nombre    nombre comercial.
 * @param direccion dirección opcional.
 * @param telefono  teléfono opcional.
 * @param activo    estado lógico.
 */
public record LocalResponse(Integer id, String nombre, String direccion, String telefono, boolean activo) {
}
