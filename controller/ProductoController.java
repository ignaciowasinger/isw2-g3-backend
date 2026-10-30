package com.example.backendprojt.controller;

import com.example.backendprojt.dto.ProductoResponse;
import com.example.backendprojt.service.ProductoService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador público de productos (RF-05).
 *
 * <p>Devuelve únicamente productos de locales activos. Los productos agotados se listan
 * con {@code disponible = false} en lugar de ocultarse.</p>
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    /**
     * Construye el controlador con el servicio de productos.
     *
     * @param productoService servicio de productos.
     */
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * Lista productos públicos con filtros opcionales.
     *
     * <p><b>GET {@code /api/productos?localId=...&amp;categoriaId=...}</b> · público.</p>
     *
     * @param localId     filtro opcional por local.
     * @param categoriaId filtro opcional por categoría.
     * @return productos activos de locales activos.
     * @response 200 listado.
     */
    @GetMapping
    public List<ProductoResponse> listar(@RequestParam(required = false) Integer localId,
                                         @RequestParam(required = false) Integer categoriaId) {
        return productoService.listarPublicos(localId, categoriaId);
    }

    /**
     * Obtiene un producto público.
     *
     * <p><b>GET {@code /api/productos/{id}}</b> · público.</p>
     *
     * @param id identificador del producto.
     * @return producto público con disponibilidad.
     * @response 200 encontrado.
     * @response 404 inexistente, inactivo o de local inactivo.
     */
    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable Integer id) {
        return productoService.obtenerPublico(id);
    }
}
