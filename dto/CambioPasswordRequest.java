package com.example.backendprojt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Solicitud de cambio de contraseña (RF-02).
 *
 * @param password nueva contraseña en texto plano.
 */
public record CambioPasswordRequest(
        @NotBlank(message = "es obligatoria")
        @Size(min = 8, max = 72, message = "debe tener entre 8 y 72 caracteres")
        String password) {

    /** @return representación segura sin la contraseña. */
    @Override
    public String toString() {
        return "CambioPasswordRequest[password=***]";
    }
}
