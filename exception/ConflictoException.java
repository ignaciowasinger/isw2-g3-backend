package com.example.backendprojt.exception;

/**
 * Excepción de conflicto con el estado actual del recurso.
 *
 * <p>Se traduce a HTTP 409, por ejemplo por stock insuficiente, nombre duplicado,
 * repartidor saturado o transición de estado no permitida.</p>
 */
public class ConflictoException extends RuntimeException {
    /**
     * @param message mensaje funcional para el cliente.
     */
    public ConflictoException(String message) {
        super(message);
    }
}
