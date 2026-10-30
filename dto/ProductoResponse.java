package com.example.backendprojt.dto;

import java.math.BigDecimal;

/**
 * Producto expuesto por la API (RF-04 y RF-05).
 *
 * <p>{@code disponible} se calcula a partir del stock: es {@code false} cuando el stock es
 * cero, aunque el producto siga listándose en la carta pública.</p>
 *
 * @param id              identificador del producto.
 * @param localId         identificador del local.
 * @param localNombre     nombre del local.
 * @param categoriaId     identificador de la categoría.
 * @param categoriaNombre nombre de la categoría.
 * @param nombre          nombre del producto.
 * @param descripcion     descripción opcional.
 * @param precio          precio unitario.
 * @param stock           unidades disponibles.
 * @param disponible      {@code true} si el stock es mayor que cero.
 * @param activo          estado lógico del producto.
 */
public record ProductoResponse(
        Integer id, Integer localId, String localNombre, Integer categoriaId, String categoriaNombre,
        String nombre, String descripcion, BigDecimal precio, Integer stock, boolean disponible,
        boolean activo) {
}
