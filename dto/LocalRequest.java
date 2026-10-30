package com.example.backendprojt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos de entrada para crear o actualizar un local (RF-03).
 *
 * <p>DTO inmutable basado en {@code record}. Los campos de contacto son opcionales y se
 * normalizan (recorte de espacios y conversión de vacíos a {@code null}) en el servicio.</p>
 *
 * @param nombre    nombre comercial único del local (sin distinguir mayúsculas).
 * @param direccion dirección física opcional.
 * @param telefono  teléfono de contacto opcional.
 * @param activo    bandera de baja lógica opcional.
 */
public record LocalRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80, message = "El nombre admite hasta 80 caracteres")
        String nombre,

        @Size(max = 120, message = "La dirección admite hasta 120 caracteres")
        String direccion,

        @Size(max = 15, message = "El teléfono admite hasta 15 caracteres")
        String telefono,

        Boolean activo) {
}
