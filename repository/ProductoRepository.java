package com.example.backendprojt.repository;

import com.example.backendprojt.entity.Producto;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio de productos.
 *
 * <p>Incluye consultas públicas y administrativas con carga anticipada de local y
 * categoría, el bloqueo pesimista ordenado usado por el motor de pedidos y la reposición
 * atómica de stock usada por la cancelación.</p>
 */
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    /**
     * Busca productos visibles públicamente.
     *
     * <p>Filtra productos y locales activos, pero no por stock: los agotados se incluyen
     * para que el cliente vea {@code disponible = false} (RF-05).</p>
     *
     * @param localId     filtro opcional por local.
     * @param categoriaId filtro opcional por categoría.
     * @return productos ordenados por nombre de categoría y de producto.
     */
    @Query("""
            select p from Producto p
            join fetch p.local l
            join fetch p.categoria c
            where p.activo = true and l.activo = true
              and (:localId is null or l.id = :localId)
              and (:categoriaId is null or c.id = :categoriaId)
            order by c.nombre, p.nombre
            """)
    List<Producto> buscarPublicos(@Param("localId") Integer localId,
                                  @Param("categoriaId") Integer categoriaId);

    /**
     * Busca todos los productos para el panel administrativo, sin filtrar por estado.
     *
     * @param localId     filtro opcional por local.
     * @param categoriaId filtro opcional por categoría.
     * @return productos ordenados por local, categoría y nombre.
     */
    @Query("""
            select p from Producto p
            join fetch p.local l
            join fetch p.categoria c
            where (:localId is null or l.id = :localId)
              and (:categoriaId is null or c.id = :categoriaId)
            order by l.nombre, c.nombre, p.nombre
            """)
    List<Producto> buscarTodos(@Param("localId") Integer localId,
                               @Param("categoriaId") Integer categoriaId);

    /**
     * Recupera un producto con local y categoría cargados.
     *
     * @param id identificador del producto.
     * @return producto encontrado, o vacío si no existe.
     */
    @Query("select p from Producto p join fetch p.local join fetch p.categoria where p.id = :id")
    Optional<Producto> buscarConRelaciones(@Param("id") Integer id);

    /**
     * Bloquea pesimistamente los productos indicados en orden ascendente de ID.
     *
     * <p>El orden estable es clave para evitar interbloqueos cuando dos pedidos
     * concurrentes incluyen los mismos productos en distinto orden (RN-02).</p>
     *
     * @param ids identificadores de producto.
     * @return productos bloqueados.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id in :ids order by p.id")
    List<Producto> bloquearPorIds(@Param("ids") Set<Integer> ids);

    /**
     * Repone stock de forma atómica dentro de la transacción de cancelación (RN-04).
     *
     * @param id       identificador del producto.
     * @param cantidad unidades a reponer.
     * @return cantidad de filas actualizadas.
     */
    @Modifying
    @Query("update Producto p set p.stock = p.stock + :cantidad where p.id = :id")
    int reponerStock(@Param("id") Integer id, @Param("cantidad") Integer cantidad);

    /**
     * Comprueba si existe un producto con ese nombre dentro de un local, ignorando
     * mayúsculas y minúsculas (control de duplicados de RF-04).
     *
     * @param localId identificador del local.
     * @param nombre  nombre a comprobar.
     * @return {@code true} si ya existe.
     */
    boolean existsByLocal_IdAndNombreIgnoreCase(Integer localId, String nombre);

    /**
     * Comprueba si otro producto del local usa el mismo nombre, excluyendo el indicado.
     *
     * @param localId identificador del local.
     * @param nombre  nombre a comprobar.
     * @param id      producto que se está actualizando.
     * @return {@code true} si otro producto ya usa el nombre.
     */
    boolean existsByLocal_IdAndNombreIgnoreCaseAndIdNot(Integer localId, String nombre, Integer id);
}
