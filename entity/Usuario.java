package com.example.backendprojt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Entidad de usuario del personal (ADMIN u OPERADOR).
 *
 * <p>La contraseña se guarda exclusivamente como hash BCrypt en
 * {@link #passwordHash}; ninguna capa de respuesta expone este valor. La baja es lógica a
 * través de {@link #activo}, y la fecha de alta se completa en el primer persistido.</p>
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    /** Identificador autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Nombre de acceso único. */
    @Column(nullable = false, unique = true, length = 40)
    private String username;

    /** Hash BCrypt de la contraseña; nunca se serializa en DTOs. */
    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    /** Nombre y apellido del usuario. */
    @Column(name = "nombre_completo", nullable = false, length = 100)
    private String nombreCompleto;

    /** Rol de seguridad del usuario. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    /** Bandera de baja lógica (RN-10). */
    @Column(nullable = false)
    private boolean activo = true;

    /** Fecha y hora de alta, completada antes del primer insert. */
    @Column(name = "fecha_alta", nullable = false)
    private LocalDateTime fechaAlta;

    /**
     * Completa la fecha de alta si no fue informada.
     */
    @PrePersist
    void prePersist() {
        if (fechaAlta == null) {
            fechaAlta = LocalDateTime.now();
        }
    }

    /** @return identificador del usuario. */
    public Integer getId() { return id; }

    /** @param id identificador del usuario. */
    public void setId(Integer id) { this.id = id; }

    /** @return nombre de acceso. */
    public String getUsername() { return username; }

    /** @param username nombre de acceso. */
    public void setUsername(String username) { this.username = username; }

    /** @return hash BCrypt de la contraseña. */
    public String getPasswordHash() { return passwordHash; }

    /** @param passwordHash hash BCrypt de la contraseña. */
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    /** @return nombre completo. */
    public String getNombreCompleto() { return nombreCompleto; }

    /** @param nombreCompleto nombre completo. */
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    /** @return rol del usuario. */
    public Rol getRol() { return rol; }

    /** @param rol rol del usuario. */
    public void setRol(Rol rol) { this.rol = rol; }

    /** @return {@code true} si el usuario está activo. */
    public boolean isActivo() { return activo; }

    /** @param activo estado lógico del usuario. */
    public void setActivo(boolean activo) { this.activo = activo; }

    /** @return fecha de alta. */
    public LocalDateTime getFechaAlta() { return fechaAlta; }

    /** @param fechaAlta fecha de alta. */
    public void setFechaAlta(LocalDateTime fechaAlta) { this.fechaAlta = fechaAlta; }
}
