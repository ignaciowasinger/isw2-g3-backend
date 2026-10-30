package com.example.backendprojt.exception;

/**
 * Excepción de recurso inexistente.
 *
 * <p>Se traduce a HTTP 404 con el formato {@code ApiError}.</p>
 */
public class RecursoNoEncontradoException extends RuntimeException {
    /**
     * @param message mensaje funcional para el cliente.
     */
    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
