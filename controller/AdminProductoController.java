package com.example.backendprojt.controller;

import com.example.backendprojt.dto.ProductoRequest;
import com.example.backendprojt.dto.ProductoResponse;
import com.example.backendprojt.service.ProductoService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador administrativo de productos (RF-04).
 *
 * <p>Todas las rutas requieren rol ADMIN. La baja es lógica (HTTP 204) y el nombre no
 * puede repetirse dentro del mismo local.</p>
 */
@RestController
@RequestMapping("/api/admin/productos")
public class AdminProductoController {

    private final ProductoService productoService;

    /**
     * Construye el controlador con el servicio de productos.
     *
     * @param productoService servicio de productos.
     */
    public AdminProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * Lista todos los productos, incluidos inactivos y agotados.
     *
     * <p><b>GET {@code /api/admin/productos?localId=...&amp;categoriaId=...}</b> · ADMIN.</p>
     *
     * @param localId     filtro opcional por local.
     * @param categoriaId filtro opcional por categoría.
     * @return productos ordenados por local, categoría y nombre.
     * @response 200 listado.
     * @response 401 no autenticado.
     * @response 403 sin rol ADMIN.
     */
    @GetMapping
    public List<ProductoResponse> listar(@RequestParam(required = false) Integer localId,
                                         @RequestParam(required = false) Integer categoriaId) {
        return productoService.listarTodos(localId, categoriaId);
    }

    /**
     * Obtiene un producto por identificador.
     *
     * <p><b>GET {@code /api/admin/productos/{id}}</b> · ADMIN.</p>
     *
     * @param id identificador del producto.
     * @return producto solicitado.
     * @response 200 encontrado.
     * @response 401 no autenticado.
     * @response 403 sin rol ADMIN.
     * @response 404 inexistente.
     */
    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable Integer id) {
        return productoService.obtener(id);
    }

    /**
     * Crea un producto.
     *
     * <p><b>POST {@code /api/admin/productos}</b> · ADMIN.</p>
     *
     * @param req datos validados.
     * @return producto creado.
     * @response 201 creado.
     * @response 400 datos inválidos.
     * @response 401 no autenticado.
     * @response 403 sin rol ADMIN.
     * @response 404 local o categoría inexistente.
     * @response 409 nombre duplicado en el local.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponse crear(@Valid @RequestBody ProductoRequest req) {
        return productoService.crear(req);
    }

    /**
     * Actualiza un producto.
     *
     * <p><b>PUT {@code /api/admin/productos/{id}}</b> · ADMIN.</p>
     *
     * @param id  identificador del producto.
     * @param req datos validados.
     * @return producto actualizado.
     * @response 200 actualizado.
     * @response 400 datos inválidos.
     * @response 401 no autenticado.
     * @response 403 sin rol ADMIN.
     * @response 404 producto, local o categoría inexistente.
     * @response 409 nombre duplicado en el local.
     */
    @PutMapping("/{id}")
    public ProductoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody ProductoRequest req) {
        return productoService.actualizar(id, req);
    }

    /**
     * Aplica baja lógica a un producto.
     *
     * <p><b>DELETE {@code /api/admin/productos/{id}}</b> · ADMIN.</p>
     *
     * @param id identificador del producto.
     * @response 204 desactivado.
     * @response 401 no autenticado.
     * @response 403 sin rol ADMIN.
     * @response 404 inexistente.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Integer id) {
        productoService.desactivar(id);
    }
}
