package com.example.backendprojt.service;

import com.example.backendprojt.dto.AvanzarPedidoRequest;
import com.example.backendprojt.dto.PedidoDetalleResponse;
import com.example.backendprojt.dto.PedidoResponse;
import com.example.backendprojt.dto.PedidoResumenResponse;
import com.example.backendprojt.dto.RepartidorEnCalleResponse;
import com.example.backendprojt.entity.EstadoPedido;
import com.example.backendprojt.entity.ItemPedido;
import com.example.backendprojt.entity.Pedido;
import com.example.backendprojt.entity.Repartidor;
import com.example.backendprojt.exception.ConflictoException;
import com.example.backendprojt.exception.RecursoNoEncontradoException;
import com.example.backendprojt.exception.SolicitudInvalidaException;
import com.example.backendprojt.repository.ItemPedidoRepository;
import com.example.backendprojt.repository.PedidoRepository;
import com.example.backendprojt.repository.ProductoRepository;
import com.example.backendprojt.repository.RepartidorRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de operaciones del personal sobre pedidos (RF-07, RF-08, RF-12, RN-03, RN-04 y RN-05).
 *
 * <p>Gestiona el listado y detalle de pedidos, las transiciones estrictas de estado, la
 * asignación de repartidores con límite de tres pedidos en camino, la cancelación con
 * reposición atómica de stock y la consulta de repartidores en calle.</p>
 */
@Service
public class GestionPedidoService {

    /** Máximo de pedidos simultáneos {@code EN_CAMINO} por repartidor (RN-03). */
    private static final int MAX_EN_CAMINO = 3;

    /** Transiciones permitidas del flujo operativo (RN-05). */
    private static final Map<EstadoPedido, EstadoPedido> SIGUIENTE = new EnumMap<>(Map.of(
            EstadoPedido.RECIBIDO, EstadoPedido.EN_PREPARACION,
            EstadoPedido.EN_PREPARACION, EstadoPedido.EN_CAMINO,
            EstadoPedido.EN_CAMINO, EstadoPedido.ENTREGADO));

    /** Estados que el listado operativo devuelve cuando no se filtra por estado. */
    private static final List<EstadoPedido> NO_TERMINADOS = List.of(
            EstadoPedido.RECIBIDO, EstadoPedido.EN_PREPARACION, EstadoPedido.EN_CAMINO);

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final ProductoRepository productoRepository;
    private final RepartidorRepository repartidorRepository;

    /**
     * Construye el servicio con sus repositorios.
     *
     * @param pedidoRepository     repositorio de pedidos.
     * @param itemPedidoRepository repositorio de líneas de pedido.
     * @param productoRepository   repositorio de productos, usado para reponer stock.
     * @param repartidorRepository repositorio de repartidores.
     */
    public GestionPedidoService(PedidoRepository pedidoRepository, ItemPedidoRepository itemPedidoRepository,
                                ProductoRepository productoRepository, RepartidorRepository repartidorRepository) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.productoRepository = productoRepository;
        this.repartidorRepository = repartidorRepository;
    }

    /**
     * Lista pedidos para el personal.
     *
     * @param estado estado opcional a filtrar; si está vacío se devuelven los pedidos no
     *               terminados (recibidos, en preparación y en camino).
     * @return resúmenes de pedidos ordenados por fecha descendente.
     * @throws SolicitudInvalidaException si el estado informado no existe.
     */
    @Transactional(readOnly = true)
    public List<PedidoResumenResponse> listar(String estado) {
        List<EstadoPedido> estados = (estado == null || estado.isBlank())
                ? NO_TERMINADOS : List.of(parsearEstado(estado));
        return pedidoRepository.buscarPorEstados(estados).stream().map(this::aResumen).toList();
    }

    /**
     * Obtiene el detalle completo de un pedido para el personal.
     *
     * @param id identificador del pedido.
     * @return detalle con datos del cliente, local, repartidor e ítems.
     * @throws RecursoNoEncontradoException si el pedido no existe.
     */
    @Transactional(readOnly = true)
    public PedidoDetalleResponse obtener(Integer id) {
        Pedido pedido = pedidoRepository.buscarDetalle(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado"));
        return aDetalle(pedido);
    }

    /**
     * Devuelve todos los repartidores activos y sus pedidos en camino (RF-12).
     *
     * <p>Incluye repartidores con cero pedidos asignados. Los pedidos sin repartidor
     * (situación anómala que las transiciones impiden) se ignoran para no romper la consulta.</p>
     *
     * @return repartidores activos ordenados por identificador.
     */
    @Transactional(readOnly = true)
    public List<RepartidorEnCalleResponse> enCalle() {
        Map<Integer, List<String>> codigosPorRepartidor = new HashMap<>();
        for (Pedido p : pedidoRepository.buscarPorEstadoConRepartidor(EstadoPedido.EN_CAMINO)) {
            if (p.getRepartidor() == null) {
                continue;
            }
            codigosPorRepartidor.computeIfAbsent(p.getRepartidor().getId(), k -> new ArrayList<>())
                    .add(p.getCodigoSeguimiento());
        }
        return repartidorRepository.findByActivoTrueOrderByIdAsc().stream()
                .map(r -> {
                    List<String> codigos = codigosPorRepartidor.getOrDefault(r.getId(), List.of());
                    return new RepartidorEnCalleResponse(r.getId(), r.getNombreCompleto(), codigos.size(), codigos);
                }).toList();
    }

    /**
     * Avanza un pedido al siguiente estado del flujo (RN-05).
     *
     * <p>El pedido se bloquea con {@code PESSIMISTIC_WRITE} para evitar transiciones
     * concurrentes. Al pasar a {@code EN_CAMINO} se valida y bloquea el repartidor, se
     * exige que esté activo y que tenga menos de tres pedidos en camino (RN-03).</p>
     *
     * @param id  identificador del pedido.
     * @param req cuerpo opcional; obligatorio con {@code repartidorId} al despachar.
     * @return detalle actualizado del pedido.
     * @throws RecursoNoEncontradoException si el pedido o el repartidor no existen.
     * @throws ConflictoException           si el pedido ya terminó o el repartidor está
     *                                      inactivo o saturado.
     * @throws SolicitudInvalidaException   si falta el repartidor al pasar a en camino.
     */
    @Transactional
    public PedidoDetalleResponse avanzar(Integer id, AvanzarPedidoRequest req) {
        Pedido pedido = bloquear(id);
        EstadoPedido siguiente = SIGUIENTE.get(pedido.getEstado());
        if (siguiente == null) {
            throw new ConflictoException("El pedido no admite más cambios");
        }
        if (siguiente == EstadoPedido.EN_CAMINO) {
            pedido.setRepartidor(validarRepartidor(req));
        }
        pedido.setEstado(siguiente);
        pedidoRepository.save(pedido);
        return aDetalle(pedido);
    }

    /**
     * Cancela un pedido y repone el stock de sus productos (RN-04).
     *
     * <p>Solo se admite en {@code RECIBIDO} o {@code EN_PREPARACION}. La operación es
     * idempotente: si el pedido ya está {@code CANCELADO} se devuelve su detalle sin
     * volver a reponer stock, de modo que un reintento del cliente no duplique unidades.
     * La reposición se ejecuta con actualizaciones atómicas dentro de la misma transacción
     * y en orden de identificador de producto para reducir el riesgo de interbloqueos.</p>
     *
     * @param id identificador del pedido.
     * @return detalle del pedido cancelado.
     * @throws RecursoNoEncontradoException si el pedido no existe.
     * @throws ConflictoException           si el pedido está en camino, entregado o ya no
     *                                      admite cancelación distinta de la idempotencia.
     */
    @Transactional
    public PedidoDetalleResponse cancelar(Integer id) {
        Pedido pedido = bloquear(id);
        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            return aDetalle(pedido);
        }
        if (pedido.getEstado() != EstadoPedido.RECIBIDO && pedido.getEstado() != EstadoPedido.EN_PREPARACION) {
            throw new ConflictoException("No se puede cancelar el pedido en este estado");
        }
        List<ItemPedido> items = itemPedidoRepository.buscarPorPedido(pedido.getId());
        items.stream().sorted(Comparator.comparing((ItemPedido i) -> i.getProducto().getId()))
                .forEach(i -> productoRepository.reponerStock(i.getProducto().getId(), i.getCantidad()));
        pedido.setEstado(EstadoPedido.CANCELADO);
        pedidoRepository.save(pedido);
        return aDetalle(pedido);
    }

    /**
     * Bloquea y recupera un pedido para operaciones de escritura.
     *
     * @param id identificador del pedido.
     * @return pedido bloqueado.
     * @throws RecursoNoEncontradoException si no existe.
     */
    private Pedido bloquear(Integer id) {
        return pedidoRepository.bloquearPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado"));
    }

    /**
     * Valida y bloquea el repartidor que se asignará al pedido.
     *
     * @param req cuerpo con el identificador del repartidor.
     * @return repartidor activo con cupo disponible.
     * @throws SolicitudInvalidaException   si falta el identificador.
     * @throws RecursoNoEncontradoException si el repartidor no existe.
     * @throws ConflictoException           si está inactivo o ya tiene tres pedidos en camino.
     */
    private Repartidor validarRepartidor(AvanzarPedidoRequest req) {
        if (req == null || req.repartidorId() == null) {
            throw new SolicitudInvalidaException("Falta repartidor");
        }
        Repartidor repartidor = repartidorRepository.bloquearPorId(req.repartidorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Repartidor no encontrado"));
        if (!repartidor.isActivo()) {
            throw new ConflictoException("Repartidor inactivo");
        }
        if (pedidoRepository.countByRepartidor_IdAndEstado(repartidor.getId(), EstadoPedido.EN_CAMINO)
                >= MAX_EN_CAMINO) {
            throw new ConflictoException("El repartidor ya tiene 3 pedidos");
        }
        return repartidor;
    }

    /**
     * Convierte y valida un estado recibido por parámetro.
     *
     * @param valor texto del estado.
     * @return estado válido.
     * @throws SolicitudInvalidaException si no corresponde a un estado conocido.
     */
    private EstadoPedido parsearEstado(String valor) {
        try {
            return EstadoPedido.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new SolicitudInvalidaException("Estado inválido");
        }
    }

    /**
     * Convierte una entidad al resumen operativo.
     *
     * @param p pedido.
     * @return resumen para listados.
     */
    private PedidoResumenResponse aResumen(Pedido p) {
        return new PedidoResumenResponse(p.getId(), p.getCodigoSeguimiento(), String.valueOf(p.getEstado()),
                p.getLocal().getNombre(), p.getCliente().getNombre(),
                p.getRepartidor() == null ? null : p.getRepartidor().getNombreCompleto(),
                p.getFechaHora(), p.getTotal());
    }

    /**
     * Convierte una entidad al detalle operativo.
     *
     * @param p pedido.
     * @return detalle con ítems y datos de contacto.
     */
    private PedidoDetalleResponse aDetalle(Pedido p) {
        List<PedidoResponse.ItemResponse> items = itemPedidoRepository.buscarPorPedido(p.getId()).stream()
                .map(i -> new PedidoResponse.ItemResponse(i.getProducto().getNombre(), i.getCantidad(),
                        i.getPrecioUnitario(),
                        i.getPrecioUnitario().multiply(BigDecimal.valueOf(i.getCantidad()))
                                .setScale(2, RoundingMode.HALF_UP)))
                .toList();
        Repartidor r = p.getRepartidor();
        return new PedidoDetalleResponse(p.getId(), p.getCodigoSeguimiento(), String.valueOf(p.getEstado()),
                p.getLocal().getId(), p.getLocal().getNombre(), p.getCliente().getNombre(),
                p.getCliente().getTelefono(), p.getDireccionEntrega(), r == null ? null : r.getId(),
                r == null ? null : r.getNombreCompleto(), p.getFechaHora(), items, p.getSubtotal(),
                p.getDescuento(), p.getCostoEnvio(), p.getTotal());
    }
}
