package com.example.backendprojt.exception;

/**
 * Regla de negocio incumplida.
 *
 * <p>Se traduce a HTTP 422 Unprocessable Entity, por ejemplo cuando un administrador
 * intenta quitarse su propio rol o un rango de fechas es inválido.</p>
 */
public class ReglaNegocioException extends RuntimeException {
    /**
     * @param message mensaje funcional para el cliente.
     */
    public ReglaNegocioException(String message) {
        super(message);
    }
}
