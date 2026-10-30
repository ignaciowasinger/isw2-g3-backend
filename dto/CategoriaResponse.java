package com.example.backendprojt.dto;

/**
 * Categoría expuesta por la API (RF-05).
 *
 * @param id     identificador de la categoría.
 * @param nombre nombre de la categoría.
 */
public record CategoriaResponse(Integer id, String nombre) {
}
