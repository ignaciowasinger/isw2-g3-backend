package com.example.backendprojt.exception;

import com.example.backendprojt.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Manejador global de excepciones de la API (RNF-08).
 *
 * <p>Traduce las excepciones de negocio y de infraestructura a respuestas homogéneas
 * {@link ApiError} con los códigos 400, 401, 403, 404, 405, 409, 415, 422 o 500. Nunca
 * expone trazas internas de Java ni mensajes de persistencia: los errores inesperados se
 * registran en el log del servidor y al cliente solo se le devuelve un mensaje genérico.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Recurso inexistente.
     *
     * @param ex  excepción capturada.
     * @param req petición actual.
     * @return 404 con el mensaje funcional.
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), req, List.of());
    }

    /**
     * Conflicto con el estado actual.
     *
     * @param ex  excepción capturada.
     * @param req petición actual.
     * @return 409 con el mensaje funcional.
     */
    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ApiError> conflicto(ConflictoException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.CONFLICT, ex.getMessage(), req, List.of());
    }

    /**
     * Solicitud funcionalmente inválida.
     *
     * @param ex  excepción capturada.
     * @param req petición actual.
     * @return 400 con el mensaje funcional.
     */
    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<ApiError> solicitudInvalida(SolicitudInvalidaException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), req, List.of());
    }

    /**
     * Regla de negocio incumplida.
     *
     * @param ex  excepción capturada.
     * @param req petición actual.
     * @return 422 con el mensaje funcional.
     */
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ApiError> reglaNegocio(ReglaNegocioException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), req, List.of());
    }

    /**
     * Errores de validación de Bean Validation.
     *
     * @param ex  excepción con el resultado de validación.
     * @param req petición actual.
     * @return 400 con la lista de campos inválidos en {@code detalles}.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        return respuesta(HttpStatus.BAD_REQUEST, "Hay datos inválidos en la solicitud", req, detalles);
    }

    /**
     * Cuerpo JSON ilegible o deserializable.
     *
     * @param ex  excepción capturada.
     * @param req petición actual.
     * @return 400 genérico.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> cuerpoIlegible(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es válido", req, List.of());
    }

    /**
     * Parámetros de ruta o query con tipo inválido o faltantes.
     *
     * @param ex  excepción capturada.
     * @param req petición actual.
     * @return 400 genérico.
     */
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiError> parametroInvalido(Exception ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, "Hay parámetros inválidos o faltantes en la solicitud", req, List.of());
    }

    /**
     * Método HTTP no soportado por la ruta.
     *
     * @param ex  excepción capturada.
     * @param req petición actual.
     * @return 405.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> metodoNoSoportado(HttpRequestMethodNotSupportedException ex,
                                                      HttpServletRequest req) {
        return respuesta(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP no permitido", req, List.of());
    }

    /**
     * Tipo de contenido no soportado.
     *
     * @param ex  excepción capturada.
     * @param req petición actual.
     * @return 415.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> tipoNoSoportado(HttpMediaTypeNotSupportedException ex,
                                                    HttpServletRequest req) {
        return respuesta(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de contenido no soportado", req, List.of());
    }

    /**
     * Ruta estática o recurso inexistente.
     *
     * @param ex  excepción capturada.
     * @param req petición actual.
     * @return 404.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> rutaNoEncontrada(NoResourceFoundException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.NOT_FOUND, "Recurso no encontrado", req, List.of());
    }

    /**
     * Credenciales incorrectas.
     *
     * @param ex  excepción capturada.
     * @param req petición actual.
     * @return 401 con mensaje genérico.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> credenciales(BadCredentialsException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos", req, List.of());
    }

    /**
     * Cualquier otra falla de autenticación (usuario inexistente, inactivo, etc.).
     *
     * @param ex  excepción capturada.
     * @param req petición actual.
     * @return 401 con mensaje genérico para no filtrar información.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> autenticacion(AuthenticationException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos", req, List.of());
    }

    /**
     * Violación de restricciones de base de datos.
     *
     * @param ex  excepción capturada.
     * @param req petición actual.
     * @return 409 genérico.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integridad(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("Violación de integridad en {}: {}", req.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return respuesta(HttpStatus.CONFLICT, "La operación entra en conflicto con datos existentes", req, List.of());
    }

    /**
     * Error inesperado.
     *
     * <p>Registra la traza completa en el log del servidor y devuelve un mensaje genérico
     * sin detalles internos (RNF-08).</p>
     *
     * @param ex  excepción capturada.
     * @param req petición actual.
     * @return 500 genérico.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> general(Exception ex, HttpServletRequest req) {
        log.error("Error no controlado en {}", req.getRequestURI(), ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado", req, List.of());
    }

    /**
     * Construye la respuesta uniforme de error.
     *
     * @param status   código HTTP.
     * @param mensaje  mensaje funcional.
     * @param req      petición actual.
     * @param detalles detalles adicionales, típicamente errores de validación.
     * @return respuesta con {@link ApiError}.
     */
    private ResponseEntity<ApiError> respuesta(HttpStatus status, String mensaje,
                                               HttpServletRequest req, List<String> detalles) {
        return ResponseEntity.status(status)
                .body(ApiError.de(status, mensaje, req.getRequestURI(), detalles));
    }
}
