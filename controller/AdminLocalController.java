package com.example.backendprojt.controller;

import com.example.backendprojt.dto.LocalRequest;
import com.example.backendprojt.dto.LocalResponse;
import com.example.backendprojt.service.LocalService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador administrativo de locales (RF-03).
 *
 * <p>Todas las rutas requieren rol ADMIN por la regla {@code /api/admin/**}. La baja es
 * lógica (HTTP 204) y no elimina la fila (RN-10).</p>
 */
@RestController
@RequestMapping("/api/admin/locales")
public class AdminLocalController {

    private final LocalService localService;

    /**
     * Construye el controlador con el servicio de locales.
     *
     * @param localService servicio de locales.
     */
    public AdminLocalController(LocalService localService) {
        this.localService = localService;
    }

    /**
     * Lista todos los locales, incluidos los inactivos.
     *
     * <p><b>GET {@code /api/admin/locales}</b> · ADMIN.</p>
     *
     * @return locales ordenados por nombre.
     * @response 200 listado.
     * @response 401 no autenticado.
     * @response 403 sin rol ADMIN.
     */
    @GetMapping
    public List<LocalResponse> listar() {
        return localService.listarTodos();
    }

    /**
     * Obtiene un local por identificador.
     *
     * <p><b>GET {@code /api/admin/locales/{id}}</b> · ADMIN.</p>
     *
     * @param id identificador del local.
     * @return local solicitado.
     * @response 200 encontrado.
     * @response 401 no autenticado.
     * @response 403 sin rol ADMIN.
     * @response 404 inexistente.
     */
    @GetMapping("/{id}")
    public LocalResponse obtener(@PathVariable Integer id) {
        return localService.obtener(id);
    }

    /**
     * Crea un local.
     *
     * <p><b>POST {@code /api/admin/locales}</b> · ADMIN.</p>
     *
     * @param req datos validados.
     * @return local creado.
     * @response 201 creado.
     * @response 400 datos inválidos.
     * @response 401 no autenticado.
     * @response 403 sin rol ADMIN.
     * @response 409 nombre duplicado.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocalResponse crear(@Valid @RequestBody LocalRequest req) {
        return localService.crear(req);
    }

    /**
     * Actualiza un local.
     *
     * <p><b>PUT {@code /api/admin/locales/{id}}</b> · ADMIN.</p>
     *
     * @param id  identificador del local.
     * @param req datos validados.
     * @return local actualizado.
     * @response 200 actualizado.
     * @response 400 datos inválidos.
     * @response 401 no autenticado.
     * @response 403 sin rol ADMIN.
     * @response 404 inexistente.
     * @response 409 nombre duplicado.
     */
    @PutMapping("/{id}")
    public LocalResponse actualizar(@PathVariable Integer id, @Valid @RequestBody LocalRequest req) {
        return localService.actualizar(id, req);
    }

    /**
     * Aplica baja lógica a un local.
     *
     * <p><b>DELETE {@code /api/admin/locales/{id}}</b> · ADMIN.</p>
     *
     * @param id identificador del local.
     * @response 204 desactivado.
     * @response 401 no autenticado.
     * @response 403 sin rol ADMIN.
     * @response 404 inexistente.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Integer id) {
        localService.desactivar(id);
    }
}
