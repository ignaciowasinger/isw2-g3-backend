package com.example.backendprojt.repository;

import com.example.backendprojt.entity.EstadoPedido;
import com.example.backendprojt.entity.Pedido;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio de pedidos.
 *
 * <p>Concentra las consultas de listado y detalle con relaciones cargadas, el bloqueo
 * pesimista del pedido para transiciones de estado y cancelación, el conteo de pedidos
 * en camino por repartidor y la búsqueda pública por código de seguimiento.</p>
 */
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    /**
     * Lista pedidos por estado con local, cliente y repartidor cargados.
     *
     * @param estados estados a incluir.
     * @return pedidos ordenados por fecha descendente.
     */
    @Query("""
            select p from Pedido p
            join fetch p.local
            join fetch p.cliente
            left join fetch p.repartidor
            where p.estado in :estados
            order by p.fechaHora desc
            """)
    List<Pedido> buscarPorEstados(@Param("estados") List<EstadoPedido> estados);

    /**
     * Recupera el detalle de un pedido con sus relaciones principales.
     *
     * @param id identificador del pedido.
     * @return pedido encontrado, o vacío si no existe.
     */
    @Query("""
            select p from Pedido p
            join fetch p.local
            join fetch p.cliente
            left join fetch p.repartidor
            where p.id = :id
            """)
    Optional<Pedido> buscarDetalle(@Param("id") Integer id);

    /**
     * Busca pedidos en un estado que tengan repartidor asignado.
     *
     * @param estado estado a filtrar.
     * @return pedidos con repartidor.
     */
    @Query("""
            select p from Pedido p
            join fetch p.repartidor
            where p.estado = :estado and p.repartidor is not null
            """)
    List<Pedido> buscarPorEstadoConRepartidor(@Param("estado") EstadoPedido estado);

    /**
     * Bloquea pesimistamente un pedido para operaciones de escritura.
     *
     * @param id identificador del pedido.
     * @return pedido bloqueado, o vacío si no existe.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pedido p where p.id = :id")
    Optional<Pedido> bloquearPorId(@Param("id") Integer id);

    /**
     * Cuenta los pedidos de un repartidor en un estado determinado (RN-03).
     *
     * @param repartidorId identificador del repartidor.
     * @param estado       estado a contar.
     * @return cantidad de pedidos.
     */
    long countByRepartidor_IdAndEstado(Integer repartidorId, EstadoPedido estado);

    /**
     * Busca un pedido por su código de seguimiento para la consulta pública.
     *
     * @param codigo código normalizado en mayúsculas.
     * @return pedido encontrado, o vacío si no existe.
     */
    @Query("""
            select p from Pedido p
            join fetch p.local
            join fetch p.cliente
            where p.codigoSeguimiento = :codigo
            """)
    Optional<Pedido> buscarPorCodigo(@Param("codigo") String codigo);

    /**
     * Comprueba si un código de seguimiento ya está en uso.
     *
     * @param codigo código a comprobar.
     * @return {@code true} si existe.
     */
    boolean existsByCodigoSeguimiento(String codigo);
}
