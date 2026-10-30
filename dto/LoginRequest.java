package com.example.backendprojt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Credenciales de acceso del personal (RF-01).
 *
 * <p>DTO inmutable. Su {@link #toString()} omite la contraseña para evitar fugas en logs.</p>
 *
 * @param username nombre de acceso.
 * @param password contraseña en texto plano, validada antes de comparar con BCrypt.
 */
public record LoginRequest(
        @NotBlank(message = "es obligatorio")
        @Size(max = 40, message = "no puede superar los 40 caracteres")
        String username,

        @NotBlank(message = "es obligatoria")
        @Size(max = 100, message = "no puede superar los 100 caracteres")
        String password) {

    /** @return representación segura sin la contraseña. */
    @Override
    public String toString() {
        return "LoginRequest[username=" + username + "]";
    }
}
