package com.example.backendprojt.security;

import com.example.backendprojt.entity.Rol;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Principal de Spring Security que representa al usuario autenticado.
 *
 * <p>Es inmutable. Traduce el {@link Rol} de dominio a la autoridad
 * {@code ROLE_<ROL>} que consumen las reglas de {@code SecurityConfig} y expone la
 * bandera {@code activo} como {@link #isEnabled()}.</p>
 */
public class UsuarioPrincipal implements UserDetails {

    private final Integer id;
    private final String username;
    private final String password;
    private final String nombreCompleto;
    private final Rol rol;
    private final boolean activo;

    /**
     * Construye el principal.
     *
     * @param id             identificador del usuario.
     * @param username       nombre de acceso.
     * @param password       hash de contraseña (puede ser vacío al reconstruir desde JWT).
     * @param nombreCompleto nombre completo.
     * @param rol            rol de dominio.
     * @param activo         estado lógico.
     */
    public UsuarioPrincipal(Integer id, String username, String password, String nombreCompleto,
                            Rol rol, boolean activo) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
        this.activo = activo;
    }

    /** @return identificador del usuario. */
    public Integer getId() { return id; }

    /** @return nombre completo. */
    public String getNombreCompleto() { return nombreCompleto; }

    /** @return rol de dominio. */
    public Rol getRol() { return rol; }

    /**
     * @return autoridades del usuario, con el prefijo {@code ROLE_}.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    /** @return hash de contraseña. */
    @Override
    public String getPassword() { return password; }

    /** @return nombre de acceso. */
    @Override
    public String getUsername() { return username; }

    /** @return {@code true}; la cuenta no expira. */
    @Override
    public boolean isAccountNonExpired() { return true; }

    /** @return {@code true}; la cuenta no se bloquea. */
    @Override
    public boolean isAccountNonLocked() { return true; }

    /** @return {@code true}; las credenciales no expiran por antigüedad. */
    @Override
    public boolean isCredentialsNonExpired() { return true; }

    /** @return {@code true} si el usuario está activo. */
    @Override
    public boolean isEnabled() { return activo; }
}
