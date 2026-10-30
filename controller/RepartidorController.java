package com.example.backendprojt.controller;

import com.example.backendprojt.dto.RepartidorResponse;
import com.example.backendprojt.service.RepartidorService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de consulta de repartidores (RF-08 y RF-12).
 *
 * <p>Requiere rol OPERADOR o ADMIN. Para la vista de repartidores en calle con sus
 * pedidos asignados existe {@code GET /api/pedidos/en-calle}.</p>
 */
@RestController
@RequestMapping("/api/repartidores")
public class RepartidorController {

    private final RepartidorService repartidorService;

    /**
     * Construye el controlador con el servicio de repartidores.
     *
     * @param repartidorService servicio de repartidores.
     */
    public RepartidorController(RepartidorService repartidorService) {
        this.repartidorService = repartidorService;
    }

    /**
     * Lista todos los repartidores.
     *
     * <p><b>GET {@code /api/repartidores}</b> · OPERADOR/ADMIN.</p>
     *
     * @return repartidores activos e inactivos.
     * @response 200 listado.
     * @response 401 no autenticado.
     * @response 403 sin permisos.
     */
    @GetMapping
    public List<RepartidorResponse> listar() {
        return repartidorService.listar();
    }
}
