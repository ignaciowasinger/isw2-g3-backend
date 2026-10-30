package com.example.backendprojt.dto;

import com.example.backendprojt.entity.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos de alta de un usuario del personal (RF-02).
 *
 * <p>DTO inmutable; la contraseña se valida entre 8 y 72 caracteres y se cifra con BCrypt
 * antes de persistir. Su {@link #toString()} no incluye la contraseña.</p>
 *
 * @param username       nombre de acceso único.
 * @param password       contraseña en texto plano.
 * @param nombreCompleto nombre y apellido.
 * @param rol            rol asignado.
 */
public record UsuarioCrearRequest(
        @NotBlank(message = "es obligatorio")
        @Size(max = 40, message = "no puede superar los 40 caracteres")
        String username,

        @NotBlank(message = "es obligatoria")
        @Size(min = 8, max = 72, message = "debe tener entre 8 y 72 caracteres")
        String password,

        @NotBlank(message = "es obligatorio")
        @Size(max = 100, message = "no puede superar los 100 caracteres")
        String nombreCompleto,

        @NotNull(message = "es obligatorio")
        Rol rol) {

    /** @return representación segura sin la contraseña. */
    @Override
    public String toString() {
        return "UsuarioCrearRequest[username=" + username + ", rol=" + rol + "]";
    }
}
