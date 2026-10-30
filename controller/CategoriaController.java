package com.example.backendprojt.controller;

import com.example.backendprojt.dto.CategoriaResponse;
import com.example.backendprojt.service.CategoriaService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador público de categorías de la carta (RF-05).
 */
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    /**
     * Construye el controlador con el servicio de categorías.
     *
     * @param categoriaService servicio de categorías.
     */
    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    /**
     * Lista todas las categorías.
     *
     * <p><b>GET {@code /api/categorias}</b> · público.</p>
     *
     * @return categorías ordenadas por identificador.
     * @response 200 listado.
     */
    @GetMapping
    public List<CategoriaResponse> listar() {
        return categoriaService.listar();
    }
}
