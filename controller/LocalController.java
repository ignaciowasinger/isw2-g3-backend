package com.example.backendprojt.controller;

import com.example.backendprojt.dto.CartaResponse;
import com.example.backendprojt.dto.LocalResponse;
import com.example.backendprojt.service.LocalService;
import com.example.backendprojt.service.ProductoService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador público de locales y cartas (RF-03 y RF-05).
 *
 * <p>Solo expone locales activos y sus productos activos. Las rutas son de lectura y no
 * requieren autenticación.</p>
 */
@RestController
@RequestMapping("/api/locales")
public class LocalController {

    private final LocalService localService;
    private final ProductoService productoService;

    /**
     * Construye el controlador con los servicios de locales y productos.
     *
     * @param localService    servicio de locales.
     * @param productoService servicio de productos y cartas.
     */
    public LocalController(LocalService localService, ProductoService productoService) {
        this.localService = localService;
        this.productoService = productoService;
    }

    /**
     * Lista los locales activos.
     *
     * <p><b>GET {@code /api/locales}</b> · público.</p>
     *
     * @return locales activos ordenados por nombre.
     * @response 200 listado.
     */
    @GetMapping
    public List<LocalResponse> listar() {
        return localService.listarActivos();
    }

    /**
     * Obtiene un local activo.
     *
     * <p><b>GET {@code /api/locales/{id}}</b> · público.</p>
     *
     * @param id identificador del local.
     * @return local activo.
     * @response 200 encontrado.
     * @response 404 inexistente o inactivo.
     */
    @GetMapping("/{id}")
    public LocalResponse obtener(@PathVariable Integer id) {
        return localService.obtenerActivo(id);
    }

    /**
     * Devuelve la carta del local agrupada por categoría.
     *
     * <p><b>GET {@code /api/locales/{id}/carta}</b> · público. Incluye productos agotados
     * con {@code disponible = false} (RF-05).</p>
     *
     * @param id identificador del local.
     * @return carta con categorías y productos.
     * @response 200 carta encontrada.
     * @response 404 local inexistente o inactivo.
     */
    @GetMapping("/{id}/carta")
    public CartaResponse carta(@PathVariable Integer id) {
        return productoService.carta(id);
    }
}
