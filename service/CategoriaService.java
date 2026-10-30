package com.example.backendprojt.service;

import com.example.backendprojt.dto.CategoriaResponse;
import com.example.backendprojt.repository.CategoriaRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de consulta de categorías de la carta.
 *
 * <p>Las categorías son datos maestros de solo lectura para la API; se listan ordenadas
 * por identificador para mantener estable el orden de la carta pública.</p>
 */
@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    /**
     * Construye el servicio con el repositorio de categorías.
     *
     * @param categoriaRepository repositorio de categorías.
     */
    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    /**
     * Lista todas las categorías.
     *
     * @return categorías ordenadas por identificador.
     */
    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll(Sort.by("id")).stream()
                .map(c -> new CategoriaResponse(c.getId(), c.getNombre())).toList();
    }
}
