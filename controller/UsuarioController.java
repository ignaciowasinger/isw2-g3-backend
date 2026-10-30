package com.example.backendprojt.controller;

import com.example.backendprojt.dto.CambioPasswordRequest;
import com.example.backendprojt.dto.UsuarioActualizarRequest;
import com.example.backendprojt.dto.UsuarioCrearRequest;
import com.example.backendprojt.dto.UsuarioResponse;
import com.example.backendprojt.security.UsuarioPrincipal;
import com.example.backendprojt.service.UsuarioService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de gestión de usuarios del personal (RF-02).
 *
 * <p>Se expone por dos rutas equivalentes ({@code /api/admin/usuarios} y
 * {@code /api/usuarios}) para compatibilidad con la app móvil; ambas quedan restringidas
 * a rol ADMIN por {@code SecurityConfig}.</p>
 */
@RestController
@RequestMapping({"/api/admin/usuarios", "/api/usuarios"})
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Construye el controlador con el servicio de usuarios.
     *
     * @param usuarioService servicio de usuarios.
     */
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Lista todos los usuarios.
     *
     * <p><b>GET {@code /api/admin/usuarios}</b> · ADMIN.</p>
     *
     * @return usuarios sin datos de contraseña.
     * @response 200 listado.
     * @response 401 no autenticado.
     * @response 403 sin rol ADMIN.
     */
    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    /**
     * Obtiene un usuario.
     *
     * <p><b>GET {@code /api/admin/usuarios/{id}}</b> · ADMIN.</p>
     *
     * @param id identificador del usuario.
     * @return usuario solicitado.
     * @response 200 encontrado.
     * @response 401 no autenticado.
     * @response 403 sin rol ADMIN.
     * @response 404 inexistente.
     */
    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Integer id) {
        return usuarioService.obtener(id);
    }

    /**
     * Crea un usuario.
     *
     * <p><b>POST {@code /api/admin/usuarios}</b> · ADMIN.</p>
     *
     * @param request datos validados.
     * @return usuario creado con ubicación en el encabezado {@code Location}.
     * @response 201 creado.
     * @response 400 datos inválidos.
     * @response 401 no autenticado.
     * @response 403 sin rol ADMIN.
     * @response 409 nombre de usuario duplicado.
     */
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioCrearRequest request) {
        UsuarioResponse creado = usuarioService.crear(request);
        return ResponseEntity.created(URI.create("/api/usuarios/" + creado.id())).body(creado);
    }

    /**
     * Actualiza un usuario y aplica las reglas de autoprotección del administrador.
     *
     * <p><b>PUT {@code /api/admin/usuarios/{id}}</b> · ADMIN.</p>
     *
     * @param id      identificador del usuario.
     * @param request datos validados.
     * @param actual  administrador autenticado.
     * @return usuario actualizado.
     * @response 200 actualizado.
     * @response 400 datos inválidos.
     * @response 401 no autenticado.
     * @response 403 sin rol ADMIN.
     * @response 404 inexistente.
     * @response 422 intento de quitarse el rol ADMIN o desactivarse.
     */
    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Integer id, @Valid @RequestBody UsuarioActualizarRequest request,
                                      @AuthenticationPrincipal UsuarioPrincipal actual) {
        return usuarioService.actualizar(id, request, actual.getId());
    }

    /**
     * Cambia la contraseña de un usuario.
     *
     * <p><b>PUT {@code /api/admin/usuarios/{id}/password}</b> · ADMIN.</p>
     *
     * @param id      identificador del usuario.
     * @param request nueva contraseña validada.
     * @return respuesta sin contenido.
     * @response 204 actualizada.
     * @response 400 datos inválidos.
     * @response 401 no autenticado.
     * @response 403 sin rol ADMIN.
     * @response 404 inexistente.
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> cambiarPassword(@PathVariable Integer id,
                                                @Valid @RequestBody CambioPasswordRequest request) {
        usuarioService.cambiarPassword(id, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Aplica baja lógica a un usuario.
     *
     * <p><b>PATCH {@code /api/admin/usuarios/{id}/desactivar}</b> · ADMIN.</p>
     *
     * @param id     identificador del usuario.
     * @param actual administrador autenticado.
     * @return usuario desactivado.
     * @response 200 desactivado.
     * @response 401 no autenticado.
     * @response 403 sin rol ADMIN.
     * @response 404 inexistente.
     * @response 422 intento de desactivarse a sí mismo (RN-09).
     */
    @PatchMapping("/{id}/desactivar")
    public UsuarioResponse desactivar(@PathVariable Integer id, @AuthenticationPrincipal UsuarioPrincipal actual) {
        return usuarioService.desactivar(id, actual.getId());
    }

    /**
     * Reactiva lógicamente un usuario.
     *
     * <p><b>PATCH {@code /api/admin/usuarios/{id}/reactivar}</b> · ADMIN.</p>
     *
     * @param id identificador del usuario.
     * @return usuario reactivado.
     * @response 200 reactivado.
     * @response 401 no autenticado.
     * @response 403 sin rol ADMIN.
     * @response 404 inexistente.
     */
    @PatchMapping("/{id}/reactivar")
    public UsuarioResponse reactivar(@PathVariable Integer id) {
        return usuarioService.reactivar(id);
    }
}
