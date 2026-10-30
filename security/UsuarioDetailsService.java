package com.example.backendprojt.security;

import com.example.backendprojt.entity.Usuario;
import com.example.backendprojt.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Adaptador entre el repositorio de usuarios y Spring Security.
 *
 * <p>Carga el usuario por nombre de acceso y lo transforma en {@link UsuarioPrincipal}.
 * Se usa tanto en el login como en cada petición autenticada por JWT.</p>
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Construye el servicio con el repositorio de usuarios.
     *
     * @param usuarioRepository repositorio de usuarios.
     */
    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Carga un usuario por nombre de acceso.
     *
     * @param username nombre de acceso.
     * @return principal de seguridad.
     * @throws UsernameNotFoundException si el usuario no existe.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario o contraseña incorrectos"));
        return new UsuarioPrincipal(usuario.getId(), usuario.getUsername(), usuario.getPasswordHash(),
                usuario.getNombreCompleto(), usuario.getRol(), usuario.isActivo());
    }
}
