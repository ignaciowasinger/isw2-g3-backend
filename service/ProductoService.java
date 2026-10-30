package com.example.backendprojt.service;

import com.example.backendprojt.dto.CartaResponse;
import com.example.backendprojt.dto.ProductoRequest;
import com.example.backendprojt.dto.ProductoResponse;
import com.example.backendprojt.entity.Categoria;
import com.example.backendprojt.entity.Local;
import com.example.backendprojt.entity.Producto;
import com.example.backendprojt.exception.ConflictoException;
import com.example.backendprojt.exception.RecursoNoEncontradoException;
import com.example.backendprojt.repository.CategoriaRepository;
import com.example.backendprojt.repository.LocalRepository;
import com.example.backendprojt.repository.ProductoRepository;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio del catálogo de productos y de la carta pública (RF-04 y RF-05).
 *
 * <p>Separa las consultas públicas (solo productos y locales activos) de las consultas
 * administrativas (incluyen inactivos y stock cero). Todos los importes se normalizan a
 * dos decimales con redondeo {@code HALF_UP}. La disponibilidad pública se deriva del
 * stock: un producto con {@code stock == 0} se lista con {@code disponible = false}.</p>
 */
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final LocalRepository localRepository;
    private final CategoriaRepository categoriaRepository;
    private final LocalService localService;

    /**
     * Construye el servicio con sus dependencias.
     *
     * @param productoRepository  repositorio de productos.
     * @param localRepository     repositorio de locales.
     * @param categoriaRepository repositorio de categorías.
     * @param localService        servicio de locales, usado para validar la carta.
     */
    public ProductoService(ProductoRepository productoRepository, LocalRepository localRepository,
                           CategoriaRepository categoriaRepository, LocalService localService) {
        this.productoRepository = productoRepository;
        this.localRepository = localRepository;
        this.categoriaRepository = categoriaRepository;
        this.localService = localService;
    }

    /**
     * Lista productos visibles públicamente.
     *
     * <p>Incluye únicamente productos y locales activos, pero no filtra por stock: los
     * productos agotados se devuelven con {@code disponible = false} (RF-05).</p>
     *
     * @param localId     filtro opcional por local.
     * @param categoriaId filtro opcional por categoría.
     * @return productos públicos ordenados por categoría y nombre.
     */
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarPublicos(Integer localId, Integer categoriaId) {
        return productoRepository.buscarPublicos(localId, categoriaId).stream().map(this::aDto).toList();
    }

    /**
     * Obtiene un producto visible públicamente.
     *
     * @param id identificador del producto.
     * @return producto activo de un local activo, con su bandera de disponibilidad.
     * @throws RecursoNoEncontradoException si no existe, está inactivo o su local lo está.
     */
    @Transactional(readOnly = true)
    public ProductoResponse obtenerPublico(Integer id) {
        Producto p = productoRepository.buscarConRelaciones(id)
                .filter(x -> x.isActivo() && x.getLocal().isActivo())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + id));
        return aDto(p);
    }

    /**
     * Construye la carta de un local agrupada por categoría (RF-05).
     *
     * <p>Mantiene el orden alfabético de categorías y productos que devuelve la consulta.
     * Los productos sin stock se incluyen con {@code disponible = false}.</p>
     *
     * @param localId identificador del local.
     * @return carta del local con sus categorías y productos.
     * @throws RecursoNoEncontradoException si el local no existe o está inactivo.
     */
    @Transactional(readOnly = true)
    public CartaResponse carta(Integer localId) {
        Local local = localService.buscarActivo(localId);
        Map<Integer, CartaResponse.CategoriaCarta> grupos = new LinkedHashMap<>();
        for (Producto p : productoRepository.buscarPublicos(localId, null)) {
            Categoria c = p.getCategoria();
            grupos.computeIfAbsent(c.getId(),
                            k -> new CartaResponse.CategoriaCarta(c.getId(), c.getNombre(), new ArrayList<>()))
                    .productos().add(aDto(p));
        }
        return new CartaResponse(local.getId(), local.getNombre(), new ArrayList<>(grupos.values()));
    }

    /**
     * Lista todos los productos para el panel administrativo.
     *
     * @param localId     filtro opcional por local.
     * @param categoriaId filtro opcional por categoría.
     * @return productos activos e inactivos.
     */
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarTodos(Integer localId, Integer categoriaId) {
        return productoRepository.buscarTodos(localId, categoriaId).stream().map(this::aDto).toList();
    }

    /**
     * Obtiene un producto para el panel administrativo, sin filtrar por estado.
     *
     * @param id identificador del producto.
     * @return producto solicitado.
     * @throws RecursoNoEncontradoException si no existe.
     */
    @Transactional(readOnly = true)
    public ProductoResponse obtener(Integer id) {
        return aDto(buscar(id));
    }

    /**
     * Crea un producto validando local, categoría y nombre único dentro del local.
     *
     * @param req datos validados del producto.
     * @return producto creado.
     * @throws RecursoNoEncontradoException si el local o la categoría no existen.
     * @throws ConflictoException           si ya existe un producto con ese nombre en el local.
     */
    @Transactional
    public ProductoResponse crear(ProductoRequest req) {
        String nombre = req.nombre().trim();
        if (productoRepository.existsByLocal_IdAndNombreIgnoreCase(req.localId(), nombre)) {
            throw new ConflictoException("Ya existe un producto con el nombre '" + nombre + "' en este local");
        }
        Producto p = new Producto();
        aplicar(p, req);
        p.setActivo(true);
        return aDto(productoRepository.save(p));
    }

    /**
     * Actualiza un producto existente.
     *
     * <p>El control de duplicados excluye al propio producto. Si {@code activo} es
     * {@code null}, se conserva el valor actual (RN-10).</p>
     *
     * @param id  identificador del producto.
     * @param req datos validados del producto.
     * @return producto actualizado.
     * @throws RecursoNoEncontradoException si el producto, local o categoría no existen.
     * @throws ConflictoException           si el nombre choca con otro producto del local.
     */
    @Transactional
    public ProductoResponse actualizar(Integer id, ProductoRequest req) {
        Producto p = buscar(id);
        String nombre = req.nombre().trim();
        if (productoRepository.existsByLocal_IdAndNombreIgnoreCaseAndIdNot(req.localId(), nombre, id)) {
            throw new ConflictoException("Ya existe un producto con el nombre '" + nombre + "' en este local");
        }
        aplicar(p, req);
        if (req.activo() != null) {
            p.setActivo(req.activo());
        }
        return aDto(productoRepository.save(p));
    }

    /**
     * Aplica una baja lógica al producto (RN-10); no elimina la fila.
     *
     * @param id identificador del producto.
     * @throws RecursoNoEncontradoException si el producto no existe.
     */
    @Transactional
    public void desactivar(Integer id) {
        Producto p = buscar(id);
        p.setActivo(false);
        productoRepository.save(p);
    }

    /**
     * Busca un producto con sus relaciones cargadas.
     *
     * @param id identificador del producto.
     * @return entidad producto.
     * @throws RecursoNoEncontradoException si no existe.
     */
    private Producto buscar(Integer id) {
        return productoRepository.buscarConRelaciones(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + id));
    }

    /**
     * Copia los datos del DTO sobre la entidad, resolviendo local y categoría.
     *
     * @param p   entidad destino.
     * @param req datos de entrada.
     * @throws RecursoNoEncontradoException si el local o la categoría no existen.
     */
    private void aplicar(Producto p, ProductoRequest req) {
        Local local = localRepository.findById(req.localId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Local no encontrado"));
        Categoria categoria = categoriaRepository.findById(req.categoriaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
        p.setLocal(local);
        p.setCategoria(categoria);
        p.setNombre(req.nombre().trim());
        p.setDescripcion(req.descripcion() == null || req.descripcion().isBlank()
                ? null : req.descripcion().trim());
        p.setPrecio(req.precio().setScale(2, RoundingMode.HALF_UP));
        p.setStock(req.stock());
    }

    /**
     * Convierte la entidad al DTO de respuesta, calculando la disponibilidad por stock.
     *
     * @param p entidad producto.
     * @return DTO de producto.
     */
    private ProductoResponse aDto(Producto p) {
        return new ProductoResponse(p.getId(), p.getLocal().getId(), p.getLocal().getNombre(),
                p.getCategoria().getId(), p.getCategoria().getNombre(), p.getNombre(), p.getDescripcion(),
                p.getPrecio(), p.getStock(), p.getStock() != null && p.getStock() > 0, p.isActivo());
    }
}
