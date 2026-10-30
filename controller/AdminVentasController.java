package com.example.backendprojt.controller;

import com.example.backendprojt.dto.VentaLocalResponse;
import com.example.backendprojt.service.VentaService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador administrativo del reporte de ventas (RF-11).
 *
 * <p>Ruta consumida por la app móvil. Devuelve solo la lista de locales; para los totales
 * generales existe {@code GET /api/ventas/por-local}. Requiere rol ADMIN.</p>
 */
@RestController
@RequestMapping("/api/admin/ventas")
public class AdminVentasController {

    private final VentaService ventaService;

    /**
     * Construye el controlador con el servicio de ventas.
     *
     * @param ventaService servicio de reportes.
     */
    public AdminVentasController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    /**
     * Lista las ventas por local en un período.
     *
     * <p><b>GET {@code /api/admin/ventas?desde=AAAA-MM-DD&amp;hasta=AAAA-MM-DD}</b> · ADMIN.</p>
     *
     * @param desde primer día del período.
     * @param hasta último día del período.
     * @return filas por local, incluidas las de cantidad cero.
     * @response 200 reporte generado.
     * @response 400 parámetros faltantes o con formato inválido.
     * @response 401 no autenticado.
     * @response 403 sin rol ADMIN.
     * @response 422 rango de fechas invertido.
     */
    @GetMapping
    public List<VentaLocalResponse> ventas(
            @RequestParam("desde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam("hasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ventaService.ventasPorLocal(desde, hasta).locales();
    }
}
