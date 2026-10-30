package com.example.backendprojt.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Datos de entrada para registrar un pedido desde el catálogo público.
 *
 * <p>Se modela como {@code record} para garantizar la inmutabilidad del DTO: una vez
 * deserializado por Jackson, sus componentes no pueden modificarse. El cliente no envía
 * precios ni totales; el backend los calcula a partir de los productos persistidos.</p>
 *
 * @param localId           identificador del local que prepara el pedido.
 * @param nombre            nombre del cliente que recibe el pedido.
 * @param telefono          teléfono de contacto; se usa para reutilizar o crear el cliente.
 * @param direccionEntrega  dirección donde se entrega el pedido.
 * @param items             líneas del pedido; debe contener al menos un producto.
 */
public record PedidoRequest(
        @NotNull(message = "El local es obligatorio")
        Integer localId,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80, message = "El nombre admite hasta 80 caracteres")
        String nombre,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "\\d{8,15}", message = "El teléfono debe tener entre 8 y 15 dígitos")
        String telefono,

        @NotBlank(message = "La dirección de entrega es obligatoria")
        @Size(max = 150, message = "La dirección admite hasta 150 caracteres")
        String direccionEntrega,

        @NotEmpty(message = "El pedido debe tener al menos un producto")
        @Valid
        List<ItemRequest> items) {

    /**
     * Línea de pedido enviada por el cliente.
     *
     * @param productoId identificador del producto solicitado.
     * @param cantidad   unidades solicitadas; debe ser al menos 1.
     */
    public record ItemRequest(
            @NotNull(message = "El producto es obligatorio")
            Integer productoId,

            @NotNull(message = "La cantidad es obligatoria")
            @Min(value = 1, message = "La cantidad debe ser 1 o más")
            Integer cantidad) {
    }
}
