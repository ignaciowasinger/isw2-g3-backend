package com.example.backendprojt.dto;

import java.util.List;

/**
 * Carta de un local agrupada por categorías (RF-05).
 *
 * @param localId     identificador del local.
 * @param localNombre nombre del local.
 * @param categorias  categorías con sus productos.
 */
public record CartaResponse(Integer localId, String localNombre, List<CategoriaCarta> categorias) {

    /**
     * Grupo de productos de una categoría.
     *
     * @param id        identificador de la categoría.
     * @param nombre    nombre de la categoría.
     * @param productos productos de la categoría, incluidos los agotados.
     */
    public record CategoriaCarta(Integer id, String nombre, List<ProductoResponse> productos) {
    }
}
