package com.example.backendprojt.dto;

import com.example.backendprojt.entity.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos de actualización de un usuario del personal (RF-02 y RN-09).
 *
 * <p>{@code activo} y {@code password} son opcionales: si {@code activo} es {@code null}
 * se conserva el estado y una contraseña vacía no se modifica.</p>
 *
 * @param nombreCompleto nuevo nombre y apellido.
 * @param rol            nuevo rol.
 * @param activo         nuevo estado lógico, opcional.
 * @param password       nueva contraseña, opcional.
 */
public record UsuarioActualizarRequest(
        @NotBlank(message = "es obligatorio")
        @Size(max = 100, message = "no puede superar los 100 caracteres")
        String nombreCompleto,

        @NotNull(message = "es obligatorio")
        Rol rol,

        Boolean activo,

        @Size(min = 8, max = 72, message = "debe tener entre 8 y 72 caracteres")
        String password) {

    /** @return representación segura sin la contraseña. */
    @Override
    public String toString() {
        return "UsuarioActualizarRequest[nombreCompleto=" + nombreCompleto
                + ", rol=" + rol + ", activo=" + activo + "]";
    }
}
