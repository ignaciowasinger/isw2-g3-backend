package com.example.backendprojt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;

/**
 * Estructura estándar de error de la API (RNF-08).
 *
 * <p>La usan tanto el manejador global de excepciones como los manejadores de seguridad
 * para 401 y 403. Los campos nulos se omiten del JSON gracias a
 * {@link JsonInclude.Include#NON_NULL}.</p>
 *
 * @param timestamp momento en que se generó el error.
 * @param status    código HTTP numérico.
 * @param error     frase estándar del código HTTP.
 * @param message   mensaje funcional para el cliente.
 * @param path      ruta de la petición que falló.
 * @param detalles  listado de errores de validación u observaciones.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(LocalDateTime timestamp, int status, String error,
                       String message, String path, List<String> detalles) {

    /**
     * Fábrica de errores uniformes.
     *
     * @param status   código HTTP.
     * @param message  mensaje funcional.
     * @param path     ruta de la petición.
     * @param detalles detalles adicionales; puede ser una lista vacía.
     * @return instancia de {@link ApiError}.
     */
    public static ApiError de(HttpStatus status, String message, String path, List<String> detalles) {
        return new ApiError(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                message, path, detalles);
    }
}
