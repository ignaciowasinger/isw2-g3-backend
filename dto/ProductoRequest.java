package com.example.backendprojt.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Datos de entrada para crear o actualizar un producto del catálogo (RF-04).
 *
 * <p>Es un {@code record} inmutable. El campo {@code activo} es opcional: en la creación
 * siempre se fuerza {@code true} y en la actualización, si es {@code null}, conserva el
 * valor persistido.</p>
 *
 * @param localId     local al que pertenece el producto.
 * @param categoriaId categoría de la carta a la que pertenece.
 * @param nombre      nombre visible del producto.
 * @param descripcion descripción opcional.
 * @param precio      precio unitario con hasta dos decimales y nunca negativo.
 * @param stock       unidades disponibles; nunca negativo.
 * @param activo      bandera de baja lógica opcional.
 */
public record ProductoRequest(
        @NotNull(message = "El local es obligatorio")
        Integer localId,

        @NotNull(message = "La categoría es obligatoria")
        Integer categoriaId,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80, message = "El nombre admite hasta 80 caracteres")
        String nombre,

        @Size(max = 200, message = "La descripción admite hasta 200 caracteres")
        String descripcion,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
        @Digits(integer = 8, fraction = 2, message = "El precio admite hasta 8 enteros y 2 decimales")
        BigDecimal precio,

        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock,

        Boolean activo) {
}
