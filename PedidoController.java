package com.example.backendprojt.controller;

import com.example.backendprojt.dto.PedidoRequest;
import com.example.backendprojt.dto.PedidoResponse;
import com.example.backendprojt.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador público de pedidos (RF-06 y RN-08).
 *
 * <p>Permite registrar un pedido sin autenticación y consultar su estado por código de
 * seguimiento. La consulta pública no devuelve teléfono, dirección ni identificadores
 * internos.</p>
 */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    /**
     * Construye el controlador con el servicio de pedidos.
     *
     * @param pedidoService servicio del motor de pedidos.
     */
    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /**
     * Registra un pedido desde el canal público.
     *
     * <p><b>POST {@code /api/pedidos}</b> · público.</p>
     *
     * @param req datos validados del pedido.
     * @return comprobante con código de seguimiento y montos calculados.
     * @response 201 pedido creado.
     * @response 400 datos inválidos, producto de otro local o inactivo.
     * @response 404 local o producto inexistente.
     * @response 409 stock insuficiente.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse crear(@Valid @RequestBody PedidoRequest req) {
        return pedidoService.crear(req);
    }

    /**
     * Consulta un pedido por código de seguimiento.
     *
     * <p><b>GET {@code /api/pedidos/seguimiento/{codigo}}</b> · público.</p>
     *
     * @param codigo código de 8 caracteres; se normaliza a mayúsculas.
     * @return comprobante público del pedido.
     * @response 200 pedido encontrado.
     * @response 404 código inexistente.
     */
    @GetMapping("/seguimiento/{codigo}")
    public PedidoResponse seguimiento(@PathVariable String codigo) {
        return pedidoService.consultarPorCodigo(codigo);
    }
}
