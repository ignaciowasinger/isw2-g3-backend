package com.example.backendprojt.controller;

import com.example.backendprojt.dto.ReporteVentasResponse;
import com.example.backendprojt.service.VentaService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador del reporte de ventas con totales generales (RF-11).
 *
 * <p>Requiere rol ADMIN por la regla {@code /api/ventas/**} de {@code SecurityConfig}.</p>
 */
@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;

    /**
     * Construye el controlador con el servicio de ventas.
     *
     * @param ventaService servicio de reportes.
     */
    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    /**
     * Calcula el reporte completo de ventas por local.
     *
     * <p><b>GET {@code /api/ventas/por-local?desde=AAAA-MM-DD&amp;hasta=AAAA-MM-DD}</b> · ADMIN.</p>
     *
     * @param desde primer día del período.
     * @param hasta último día del período.
     * @return filas por local y totales de pedidos y monto.
     * @response 200 reporte generado.
     * @response 400 parámetros faltantes o con formato inválido.
     * @response 401 no autenticado.
     * @response 403 sin rol ADMIN.
     * @response 422 rango de fechas invertido.
     */
    @GetMapping("/por-local")
    public ReporteVentasResponse ventasPorLocal(
            @RequestParam("desde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam("hasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ventaService.ventasPorLocal(desde, hasta);
    }
}
