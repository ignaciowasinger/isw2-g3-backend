package com.example.backendprojt.repository;

import com.example.backendprojt.entity.Repartidor;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio de repartidores.
 *
 * <p>Incluye el listado de activos y el bloqueo pesimista de la fila del repartidor
 * previo a validar su cupo de pedidos en camino (RN-03).</p>
 */
public interface RepartidorRepository extends JpaRepository<Repartidor, Integer> {

    /**
     * Lista los repartidores activos ordenados por identificador.
     *
     * @return repartidores activos.
     */
    List<Repartidor> findByActivoTrueOrderByIdAsc();

    /**
     * Bloquea pesimistamente un repartidor para validar y asignar cupo.
     *
     * @param id identificador del repartidor.
     * @return repartidor bloqueado, o vacío si no existe.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Repartidor r where r.id = :id")
    Optional<Repartidor> bloquearPorId(@Param("id") Integer id);
}
