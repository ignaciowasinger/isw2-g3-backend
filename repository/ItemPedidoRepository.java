package com.example.backendprojt.repository;

import com.example.backendprojt.entity.ItemPedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio de líneas de pedido.
 *
 * <p>Las líneas se consultan siempre con su producto cargado para poder armar los DTOs y
 * reponer stock sin consultas adicionales por elemento.</p>
 */
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Integer> {

    /**
     * Lista las líneas de un pedido con el producto cargado.
     *
     * @param pedidoId identificador del pedido.
     * @return líneas ordenadas por identificador.
     */
    @Query("""
            select i from ItemPedido i
            join fetch i.producto
            where i.pedido.id = :pedidoId
            order by i.id
            """)
    List<ItemPedido> buscarPorPedido(@Param("pedidoId") Integer pedidoId);
}
