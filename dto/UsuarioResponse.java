package com.example.backendprojt.dto;

import com.example.backendprojt.entity.Rol;
import com.example.backendprojt.entity.Usuario;
import java.time.LocalDateTime;

/**
 * Usuario expuesto por la API (RF-02).
 *
 * <p>No incluye contraseña ni hash: la conversión desde la entidad solo copia campos
 * públicos.</p>
 *
 * @param id             identificador del usuario.
 * @param username       nombre de acceso.
 * @param nombreCompleto nombre y apellido.
 * @param rol            rol asignado.
 * @param activo         estado lógico.
 * @param fechaAlta      fecha de alta.
 */
public record UsuarioResponse(Integer id, String username, String nombreCompleto,
                              Rol rol, boolean activo, LocalDateTime fechaAlta) {

    /**
     * Convierte una entidad usuario al DTO seguro.
     *
     * @param u entidad de origen.
     * @return DTO sin credenciales.
     */
    public static UsuarioResponse desde(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getUsername(), u.getNombreCompleto(),
                u.getRol(), u.isActivo(), u.getFechaAlta());
    }
}
