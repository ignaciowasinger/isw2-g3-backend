package com.example.backendprojt.exception;

/**
 * Solicitud inválida desde el punto de vista funcional.
 *
 * <p>Se traduce a HTTP 400, por ejemplo cuando un producto no pertenece al local del
 * pedido o falta el repartidor al despachar.</p>
 */
public class SolicitudInvalidaException extends RuntimeException {
    /**
     * @param message mensaje funcional para el cliente.
     */
    public SolicitudInvalidaException(String message) {
        super(message);
    }
}
