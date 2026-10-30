package com.example.backendprojt.entity;

/**
 * Roles de seguridad del personal.
 *
 * <p>Spring Security los expone como autoridades con el prefijo {@code ROLE_} (por
 * ejemplo {@code ROLE_ADMIN}), tal como los consume {@code SecurityConfig}.</p>
 */
public enum Rol {
    /** Administrador: gestión de usuarios, catálogo y reportes. */
    ADMIN,
    /** Operador: gestión diaria de pedidos y repartidores. */
    OPERADOR
}
