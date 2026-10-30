package com.example.backendprojt.service;

import com.example.backendprojt.dto.PedidoRequest;
import com.example.backendprojt.dto.PedidoResponse;
import com.example.backendprojt.entity.Cliente;
import com.example.backendprojt.entity.EstadoPedido;
import com.example.backendprojt.entity.ItemPedido;
import com.example.backendprojt.entity.Local;
import com.example.backendprojt.entity.Pedido;
import com.example.backendprojt.entity.Producto;
import com.example.backendprojt.exception.ConflictoException;
import com.example.backendprojt.exception.RecursoNoEncontradoException;
import com.example.backendprojt.exception.SolicitudInvalidaException;
import com.example.backendprojt.repository.ClienteRepository;
import com.example.backendprojt.repository.ItemPedidoRepository;
import com.example.backendprojt.repository.LocalRepository;
import com.example.backendprojt.repository.PedidoRepository;
import com.example.backendprojt.repository.ProductoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio del motor transaccional de pedidos (RF-06, RN-01, RN-02, RN-06, RN-07 y RN-08).
 *
 * <p>Concentra la creación de pedidos desde el canal público y la consulta de seguimiento
 * por código. La creación se ejecuta en una única transacción: valida local, productos y
 * stock, bloquea las filas de producto en orden de identificador, descuenta existencias,
 * reutiliza o crea el cliente por teléfono, calcula los importes en el backend y persiste
 * el pedido con su código de seguimiento único.</p>
 */
@Service
public class PedidoService {

    /** Subtotal estrictamente mayor a este valor habilita el descuento (RN-06). */
    private static final BigDecimal UMBRAL_DESCUENTO = new BigDecimal("20000.00");

    /** Porcentaje de descuento aplicado sobre el subtotal. */
    private static final BigDecimal PORCENTAJE_DESCUENTO = new BigDecimal("0.10");

    /** Costo de envío fijo del pedido. */
    private static final BigDecimal COSTO_ENVIO = new BigDecimal("1500.00");

    /** Alfabeto permitido para el código de seguimiento (sin caracteres ambiguos). */
    private static final String ALFABETO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /** Longitud exigida del código de seguimiento (RN-08). */
    private static final int LARGO_CODIGO = 8;

    /** Cantidad máxima de intentos para encontrar un código libre antes de fallar. */
    private static final int MAX_INTENTOS_CODIGO = 20;

    /** Generador criptográficamente seguro para el código de seguimiento. */
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final ClienteRepository clienteRepository;
    private final LocalRepository localRepository;
    private final ProductoRepository productoRepository;

    /**
     * Construye el servicio con sus repositorios.
     *
     * @param pedidoRepository      repositorio de pedidos.
     * @param itemPedidoRepository  repositorio de líneas de pedido.
     * @param clienteRepository     repositorio de clientes.
     * @param localRepository       repositorio de locales.
     * @param productoRepository    repositorio de productos.
     */
    public PedidoService(PedidoRepository pedidoRepository, ItemPedidoRepository itemPedidoRepository,
                         ClienteRepository clienteRepository, LocalRepository localRepository,
                         ProductoRepository productoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.clienteRepository = clienteRepository;
        this.localRepository = localRepository;
        this.productoRepository = productoRepository;
    }

    /**
     * Registra un pedido público y devuelve su comprobante de seguimiento.
     *
     * <p>Lógica aplicada:</p>
     * <ol>
     *   <li>Valida que el local exista y esté activo (RN-01).</li>
     *   <li>Rechaza productos repetidos en la misma solicitud.</li>
     *   <li>Bloquea los productos con {@code PESSIMISTIC_WRITE} ordenados por ID para
     *       serializar compras concurrentes y evitar sobreventa (RN-02).</li>
     *   <li>Verifica que cada producto exista, pertenezca al local, esté activo y tenga
     *       stock suficiente (RN-01).</li>
     *   <li>Reutiliza el cliente por teléfono o lo crea automáticamente (RN-07).</li>
     *   <li>Descuenta stock, captura el precio unitario vigente (RN-06) y calcula subtotal,
     *       descuento del 10 % cuando el subtotal supera estrictamente $20.000,00 y envío
     *       fijo de $1.500,00 con redondeo {@code HALF_UP} a dos decimales.</li>
     *   <li>Genera un código alfanumérico único de 8 caracteres (RN-08) y persiste el
     *       pedido junto con sus líneas.</li>
     * </ol>
     *
     * @param req datos validados del pedido.
     * @return comprobante público del pedido creado, sin datos personales sensibles.
     * @throws RecursoNoEncontradoException si el local o algún producto no existen.
     * @throws SolicitudInvalidaException   si el local está inactivo, hay productos
     *                                      repetidos, de otro local o inactivos.
     * @throws ConflictoException           si no hay stock suficiente.
     */
    @Transactional
    public PedidoResponse crear(PedidoRequest req) {
        Local local = localRepository.findById(req.localId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Local no encontrado"));
        if (!local.isActivo()) {
            throw new SolicitudInvalidaException("El local no está disponible");
        }

        Set<Integer> ids = new LinkedHashSet<>();
        for (PedidoRequest.ItemRequest item : req.items()) {
            if (!ids.add(item.productoId())) {
                throw new SolicitudInvalidaException("Producto repetido");
            }
        }

        Map<Integer, Producto> productos = productoRepository.bloquearPorIds(ids).stream()
                .collect(Collectors.toMap(Producto::getId, Function.identity()));

        for (PedidoRequest.ItemRequest item : req.items()) {
            Producto p = productos.get(item.productoId());
            if (p == null) {
                throw new RecursoNoEncontradoException("Producto no encontrado");
            }
            if (!p.getLocal().getId().equals(local.getId())) {
                throw new SolicitudInvalidaException("Producto no es del local");
            }
            if (!p.isActivo()) {
                throw new SolicitudInvalidaException("Producto inactivo");
            }
            if (item.cantidad() > p.getStock()) {
                throw new ConflictoException("Stock insuficiente");
            }
        }

        String telefono = req.telefono().trim();
        Cliente cliente = clienteRepository.findByTelefono(telefono).orElseGet(() -> {
            Cliente nuevo = new Cliente();
            nuevo.setNombre(req.nombre().trim());
            nuevo.setTelefono(telefono);
            return clienteRepository.save(nuevo);
        });

        BigDecimal subtotal = BigDecimal.ZERO;
        List<ItemPedido> items = new ArrayList<>();
        for (PedidoRequest.ItemRequest item : req.items()) {
            Producto p = productos.get(item.productoId());
            p.setStock(p.getStock() - item.cantidad());
            BigDecimal precio = p.getPrecio().setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(precio.multiply(BigDecimal.valueOf(item.cantidad())));

            ItemPedido ip = new ItemPedido();
            ip.setProducto(p);
            ip.setCantidad(item.cantidad());
            ip.setPrecioUnitario(precio);
            items.add(ip);
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal descuento = (subtotal.compareTo(UMBRAL_DESCUENTO) > 0)
                ? subtotal.multiply(PORCENTAJE_DESCUENTO).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2);
        BigDecimal total = subtotal.subtract(descuento).add(COSTO_ENVIO).setScale(2, RoundingMode.HALF_UP);

        Pedido pedido = new Pedido();
        pedido.setCodigoSeguimiento(generarCodigoUnico());
        pedido.setCliente(cliente);
        pedido.setLocal(local);
        pedido.setDireccionEntrega(req.direccionEntrega().trim());
        pedido.setFechaHora(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.RECIBIDO);
        pedido.setSubtotal(subtotal);
        pedido.setDescuento(descuento);
        pedido.setCostoEnvio(COSTO_ENVIO);
        pedido.setTotal(total);
        pedido = pedidoRepository.save(pedido);

        for (ItemPedido ip : items) {
            ip.setPedido(pedido);
        }
        itemPedidoRepository.saveAll(items);

        return aDto(pedido, items);
    }

    /**
     * Consulta pública de un pedido por su código de seguimiento (RN-08).
     *
     * <p>El código se normaliza a mayúsculas y se recorta. La respuesta usa
     * {@link PedidoResponse}, que deliberadamente no incluye teléfono, dirección ni
     * identificadores internos del cliente o del pedido.</p>
     *
     * @param codigo código de seguimiento ingresado por el cliente.
     * @return comprobante público del pedido.
     * @throws RecursoNoEncontradoException si el código no existe.
     */
    @Transactional(readOnly = true)
    public PedidoResponse consultarPorCodigo(String codigo) {
        String normalizado = codigo == null ? "" : codigo.trim().toUpperCase();
        Pedido pedido = pedidoRepository.buscarPorCodigo(normalizado)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado"));
        return aDto(pedido, itemPedidoRepository.buscarPorPedido(pedido.getId()));
    }

    /**
     * Genera un código de seguimiento aleatorio y verifica que no exista.
     *
     * @return código único de 8 caracteres alfanuméricos en mayúsculas.
     * @throws IllegalStateException si no se encuentra un código libre tras los intentos
     *                               configurados; el llamador lo traduce a HTTP 500.
     */
    private String generarCodigoUnico() {
        for (int intento = 0; intento < MAX_INTENTOS_CODIGO; intento++) {
            StringBuilder sb = new StringBuilder(LARGO_CODIGO);
            for (int i = 0; i < LARGO_CODIGO; i++) {
                sb.append(ALFABETO.charAt(RANDOM.nextInt(ALFABETO.length())));
            }
            String codigo = sb.toString();
            if (!pedidoRepository.existsByCodigoSeguimiento(codigo)) {
                return codigo;
            }
        }
        throw new IllegalStateException("Error al generar código");
    }

    /**
     * Convierte una entidad {@link Pedido} y sus líneas al DTO público.
     *
     * @param pedido pedido persistido.
     * @param items  líneas del pedido.
     * @return DTO sin información personal ni identificadores internos.
     */
    private PedidoResponse aDto(Pedido pedido, List<ItemPedido> items) {
        List<PedidoResponse.ItemResponse> itemsDto = items.stream()
                .map(i -> new PedidoResponse.ItemResponse(i.getProducto().getNombre(), i.getCantidad(),
                        i.getPrecioUnitario(),
                        i.getPrecioUnitario().multiply(BigDecimal.valueOf(i.getCantidad()))
                                .setScale(2, RoundingMode.HALF_UP)))
                .toList();
        return new PedidoResponse(pedido.getCodigoSeguimiento(), String.valueOf(pedido.getEstado()),
                pedido.getLocal().getNombre(), pedido.getFechaHora(), itemsDto, pedido.getSubtotal(),
                pedido.getDescuento(), pedido.getCostoEnvio(), pedido.getTotal());
    }
}
