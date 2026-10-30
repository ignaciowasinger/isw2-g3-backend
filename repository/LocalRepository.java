package com.example.backendprojt.repository;

import com.example.backendprojt.entity.Local;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de locales.
 *
 * <p>Ofrece el listado público de activos y los controles de unicidad de nombre sin
 * distinguir mayúsculas, necesarios para RF-03.</p>
 */
public interface LocalRepository extends JpaRepository<Local, Integer> {

    /**
     * Lista los locales activos para el catálogo público.
     *
     * @return locales activos ordenados por nombre.
     */
    List<Local> findByActivoTrueOrderByNombreAsc();

    /**
     * Comprueba si existe un local con ese nombre, ignorando mayúsculas.
     *
     * @param nombre nombre a comprobar.
     * @return {@code true} si existe.
     */
    boolean existsByNombreIgnoreCase(String nombre);

    /**
     * Comprueba si otro local usa el mismo nombre, excluyendo el indicado.
     *
     * @param nombre nombre a comprobar.
     * @param id     local que se está actualizando.
     * @return {@code true} si otro local ya usa el nombre.
     */
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Integer id);
}
