package com.example.backendprojt.controller;

import com.example.backendprojt.dto.AvanzarPedidoRequest;
import com.example.backendprojt.dto.PedidoDetalleResponse;
import com.example.backendprojt.dto.PedidoResumenResponse;
import com.example.backendprojt.dto.RepartidorEnCalleResponse;
import com.example.backendprojt.service.GestionPedidoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de operaciones del personal sobre pedidos (RF-07, RF-08 y RF-12).
 *
 * <p>Todos los endpoints requieren rol OPERADOR o ADMIN, según las reglas de
 * {@code SecurityConfig}.</p>
 */
@RestController
@RequestMapping("/api/pedidos")
public class GestionPedidoController {

    private final GestionPedidoService gestionPedidoService;

    /**
     * Construye el controlador con el servicio de gestión.
     *
     * @param gestionPedidoService servicio de operaciones.
     */
    public GestionPedidoController(GestionPedidoService gestionPedidoService) {
        this.gestionPedidoService = gestionPedidoService;
    }

    /**
     * Lista pedidos, opcionalmente filtrados por estado.
     *
     * <p><b>GET {@code /api/pedidos?estado=...}</b> · OPERADOR/ADMIN.</p>
     *
     * @param estado estado opcional; sin filtro devuelve los no terminados.
     * @return resúmenes de pedidos.
     * @response 200 listado.
     * @response 400 estado inválido.
     * @response 401 no autenticado.
     * @response 403 sin permisos.
     */
    @GetMapping
    public List<PedidoResumenResponse> listar(@RequestParam(required = false) String estado) {
        return gestionPedidoService.listar(estado);
    }

    /**
     * Lista repartidores activos con sus pedidos en camino.
     *
     * <p><b>GET {@code /api/pedidos/en-calle}</b> · OPERADOR/ADMIN.</p>
     *
     * @return repartidores activos, incluidos los que tienen cero pedidos.
     * @response 200 listado.
     * @response 401 no autenticado.
     * @response 403 sin permisos.
     */
    @GetMapping("/en-calle")
    public List<RepartidorEnCalleResponse> enCalle() {
        return gestionPedidoService.enCalle();
    }

    /**
     * Obtiene el detalle de un pedido.
     *
     * <p><b>GET {@code /api/pedidos/{id}}</b> · OPERADOR/ADMIN.</p>
     *
     * @param id identificador del pedido.
     * @return detalle con datos de contacto y repartidor.
     * @response 200 pedido encontrado.
     * @response 401 no autenticado.
     * @response 403 sin permisos.
     * @response 404 pedido inexistente.
     */
    @GetMapping("/{id}")
    public PedidoDetalleResponse obtener(@PathVariable Integer id) {
        return gestionPedidoService.obtener(id);
    }

    /**
     * Avanza un pedido al siguiente estado del flujo.
     *
     * <p><b>POST {@code /api/pedidos/{id}/avanzar}</b> · OPERADOR/ADMIN. Requiere
     * {@code repartidorId} al pasar de {@code EN_PREPARACION} a {@code EN_CAMINO}.</p>
     *
     * @param id  identificador del pedido.
     * @param req cuerpo opcional con el repartidor.
     * @return detalle actualizado.
     * @response 200 transición aplicada.
     * @response 400 falta el repartidor o el cuerpo es inválido.
     * @response 401 no autenticado.
     * @response 403 sin permisos.
     * @response 404 pedido o repartidor inexistente.
     * @response 409 transición no permitida, repartidor inactivo o sin cupo.
     */
    @PostMapping("/{id}/avanzar")
    public PedidoDetalleResponse avanzar(@PathVariable Integer id,
                                         @Valid @RequestBody(required = false) AvanzarPedidoRequest req) {
        return gestionPedidoService.avanzar(id, req);
    }

    /**
     * Cancela un pedido en estado recibido o en preparación.
     *
     * <p><b>POST {@code /api/pedidos/{id}/cancelar}</b> · OPERADOR/ADMIN. Es idempotente:
     * repetir la operación sobre un pedido ya cancelado devuelve su detalle sin reponer
     * stock nuevamente (RN-04).</p>
     *
     * @param id identificador del pedido.
     * @return detalle del pedido cancelado.
     * @response 200 cancelación aplicada o ya existente.
     * @response 401 no autenticado.
     * @response 403 sin permisos.
     * @response 404 pedido inexistente.
     * @response 409 el pedido ya no admite cancelación.
     */
    @PostMapping("/{id}/cancelar")
    public PedidoDetalleResponse cancelar(@PathVariable Integer id) {
        return gestionPedidoService.cancelar(id);
    }
}
