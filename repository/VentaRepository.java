package com.example.backendprojt.repository;

import com.example.backendprojt.entity.EstadoPedido;
import com.example.backendprojt.entity.Pedido;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio de solo lectura para el reporte de ventas (RF-11).
 *
 * <p>Se mantiene separado de {@link PedidoRepository} para no mezclar la operación diaria
 * con consultas agregadas de reportes.</p>
 */
public interface VentaRepository extends Repository<Pedido, Integer> {

    /**
     * Calcula una fila por local con la cantidad de pedidos entregados y el monto vendido.
     *
     * <p>El resultado de cada fila es {@code [id, nombre, cantidad, suma]} donde la suma es
     * {@code subtotal - descuento} (sin costo de envío). Los filtros se aplican en el
     * {@code ON} del {@code LEFT JOIN} para que los locales sin ventas aparezcan en cero.
     * El rango es semiabierto {@code [desde, hasta)}.</p>
     *
     * @param estado estado de pedido a considerar (normalmente {@code ENTREGADO}).
     * @param desde  inicio inclusive del período.
     * @param hasta  fin exclusivo del período.
     * @return filas agregadas ordenadas por nombre de local.
     */
    @Query("""
            select l.id, l.nombre, count(p.id), sum(p.subtotal - p.descuento)
            from Local l
            left join Pedido p on p.local = l
                 and p.estado = :estado
                 and p.fechaHora >= :desde
                 and p.fechaHora < :hasta
            group by l.id, l.nombre
            order by l.nombre
            """)
    List<Object[]> ventasPorLocal(@Param("estado") EstadoPedido estado,
                                  @Param("desde") LocalDateTime desde,
                                  @Param("hasta") LocalDateTime hasta);
}
