package com.example.backendprojt.service;

import com.example.backendprojt.dto.CambioPasswordRequest;
import com.example.backendprojt.dto.UsuarioActualizarRequest;
import com.example.backendprojt.dto.UsuarioCrearRequest;
import com.example.backendprojt.dto.UsuarioResponse;
import com.example.backendprojt.entity.Rol;
import com.example.backendprojt.entity.Usuario;
import com.example.backendprojt.exception.ConflictoException;
import com.example.backendprojt.exception.RecursoNoEncontradoException;
import com.example.backendprojt.exception.ReglaNegocioException;
import com.example.backendprojt.repository.UsuarioRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de gestión de usuarios del personal (RF-02, RN-09 y RN-10).
 *
 * <p>Implementa el alta con contraseña BCrypt, la actualización de datos y rol, el cambio
 * de contraseña y la baja/reactivación lógica. Ningún DTO de salida incluye el hash.
 * Las reglas de autoprotección impiden que un administrador se quite a sí mismo el rol
 * ADMIN o se desactive (RN-09).</p>
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Construye el servicio con sus dependencias.
     *
     * @param usuarioRepository repositorio de usuarios.
     * @param passwordEncoder   codificador BCrypt.
     */
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Lista todos los usuarios sin exponer credenciales.
     *
     * @return usuarios ordenados por nombre de acceso.
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll(Sort.by("username")).stream()
                .map(UsuarioResponse::desde).toList();
    }

    /**
     * Obtiene un usuario por identificador.
     *
     * @param id identificador del usuario.
     * @return usuario solicitado.
     * @throws RecursoNoEncontradoException si no existe.
     */
    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Integer id) {
        return UsuarioResponse.desde(buscar(id));
    }

    /**
     * Crea un usuario con contraseña cifrada.
     *
     * @param request datos validados del usuario.
     * @return usuario creado.
     * @throws ConflictoException si el nombre de usuario ya existe.
     */
    @Transactional
    public UsuarioResponse crear(UsuarioCrearRequest request) {
        String username = request.username().trim();
        if (usuarioRepository.existsByUsername(username)) {
            throw new ConflictoException("El nombre de usuario ya está en uso");
        }
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setNombreCompleto(request.nombreCompleto().trim());
        usuario.setRol(request.rol());
        usuario.setActivo(true);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    /**
     * Actualiza nombre, rol, estado y opcionalmente contraseña de un usuario.
     *
     * <p>Si el usuario editado es el mismo que realiza la operación, se impide quitarle
     * el rol ADMIN o desactivarlo (RN-09). El campo {@code activo} nulo conserva el estado
     * actual y una contraseña vacía no se modifica.</p>
     *
     * @param id               identificador del usuario a actualizar.
     * @param request          datos validados.
     * @param idUsuarioActual  identificador del administrador autenticado.
     * @return usuario actualizado.
     * @throws RecursoNoEncontradoException si el usuario no existe.
     * @throws ReglaNegocioException        si se intenta quitar el rol ADMIN o desactivar
     *                                      la propia cuenta.
     */
    @Transactional
    public UsuarioResponse actualizar(Integer id, UsuarioActualizarRequest request, Integer idUsuarioActual) {
        Usuario usuario = buscar(id);
        boolean esUnoMismo = usuario.getId().equals(idUsuarioActual);
        if (esUnoMismo && usuario.getRol() == Rol.ADMIN && request.rol() != Rol.ADMIN) {
            throw new ReglaNegocioException("Un administrador no puede quitarse a sí mismo el rol de administrador");
        }
        if (esUnoMismo && Boolean.FALSE.equals(request.activo())) {
            throw new ReglaNegocioException("Un administrador no puede desactivarse a sí mismo");
        }
        usuario.setNombreCompleto(request.nombreCompleto().trim());
        usuario.setRol(request.rol());
        if (request.activo() != null && !esUnoMismo) {
            usuario.setActivo(request.activo());
        }
        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    /**
     * Cambia la contraseña de un usuario.
     *
     * @param id      identificador del usuario.
     * @param request nueva contraseña validada.
     * @throws RecursoNoEncontradoException si el usuario no existe.
     */
    @Transactional
    public void cambiarPassword(Integer id, CambioPasswordRequest request) {
        Usuario usuario = buscar(id);
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuarioRepository.save(usuario);
    }

    /**
     * Aplica una baja lógica a un usuario.
     *
     * @param id              identificador del usuario.
     * @param idUsuarioActual identificador del administrador autenticado.
     * @return usuario desactivado.
     * @throws RecursoNoEncontradoException si el usuario no existe.
     * @throws ReglaNegocioException        si el administrador intenta desactivarse a sí mismo.
     */
    @Transactional
    public UsuarioResponse desactivar(Integer id, Integer idUsuarioActual) {
        Usuario usuario = buscar(id);
        if (usuario.getId().equals(idUsuarioActual)) {
            throw new ReglaNegocioException("Un administrador no puede desactivarse a sí mismo");
        }
        usuario.setActivo(false);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    /**
     * Reactiva lógicamente un usuario.
     *
     * @param id identificador del usuario.
     * @return usuario reactivado.
     * @throws RecursoNoEncontradoException si el usuario no existe.
     */
    @Transactional
    public UsuarioResponse reactivar(Integer id) {
        Usuario usuario = buscar(id);
        usuario.setActivo(true);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    /**
     * Busca un usuario o falla con 404.
     *
     * @param id identificador del usuario.
     * @return entidad usuario.
     * @throws RecursoNoEncontradoException si no existe.
     */
    private Usuario buscar(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario con id " + id + " no encontrado"));
    }
}
