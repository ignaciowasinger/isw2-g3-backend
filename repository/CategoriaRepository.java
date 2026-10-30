package com.example.backendprojt.repository;

import com.example.backendprojt.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de categorías de la carta.
 *
 * <p>Las categorías son datos maestros cargados por {@code data.sql}; el sistema solo
 * expone su consulta pública y su uso al crear productos.</p>
 */
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
}
