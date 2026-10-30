package com.example.backendprojt.service;

import com.example.backendprojt.dto.ReporteVentasResponse;
import com.example.backendprojt.dto.VentaLocalResponse;
import com.example.backendprojt.entity.EstadoPedido;
import com.example.backendprojt.exception.ReglaNegocioException;
import com.example.backendprojt.repository.VentaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio del reporte de ventas por local (RF-11).
 *
 * <p>Considera únicamente pedidos {@code ENTREGADO} cuya fecha esté dentro del período,
 * con ambos extremos incluidos. El monto vendido es {@code subtotal - descuento}; el
 * costo de envío no se imputa al local. Los locales sin ventas se devuelven en cero.</p>
 */
@Service
public class VentaService {

    /** Importe cero con escala de dos decimales. */
    private static final BigDecimal CERO = BigDecimal.ZERO.setScale(2);

    private final VentaRepository ventaRepository;

    /**
     * Construye el servicio con el repositorio de reportes.
     *
     * @param ventaRepository repositorio de consultas agregadas.
     */
    public VentaService(VentaRepository ventaRepository) {
        this.ventaRepository = ventaRepository;
    }

    /**
     * Calcula las ventas por local en un rango de fechas inclusivo.
     *
     * @param desde primer día del período.
     * @param hasta último día del período.
     * @return reporte con filas por local y totales generales.
     * @throws ReglaNegocioException si {@code desde} es posterior a {@code hasta} (HTTP 422).
     */
    @Transactional(readOnly = true)
    public ReporteVentasResponse ventasPorLocal(LocalDate desde, LocalDate hasta) {
        if (desde.isAfter(hasta)) {
            throw new ReglaNegocioException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        List<VentaLocalResponse> locales = ventaRepository
                .ventasPorLocal(EstadoPedido.ENTREGADO, desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay())
                .stream()
                .map(fila -> new VentaLocalResponse(
                        (Integer) fila[0],
                        (String) fila[1],
                        ((Number) fila[2]).longValue(),
                        fila[3] == null ? CERO : ((BigDecimal) fila[3]).setScale(2, RoundingMode.HALF_UP)))
                .toList();

        long totalPedidos = locales.stream().mapToLong(VentaLocalResponse::cantidadPedidos).sum();
        BigDecimal totalVendido = locales.stream()
                .map(VentaLocalResponse::montoVendido)
                .reduce(CERO, BigDecimal::add);
        return new ReporteVentasResponse(desde, hasta, locales, totalPedidos, totalVendido);
    }
}
