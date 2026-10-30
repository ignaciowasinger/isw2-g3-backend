package com.example.backendprojt.dto;

/**
 * Respuesta del login (RF-01).
 *
 * <p>DTO inmutable. {@code nombreCompleto} es el nombre histórico y {@code nombre} el
 * alias que consume la app móvil; ambos contienen el mismo valor para compatibilidad.</p>
 *
 * @param token          JWT firmado con vigencia de ocho horas.
 * @param nombreCompleto nombre completo del usuario.
 * @param nombre         alias móvil del nombre completo.
 * @param rol            rol del usuario ({@code ADMIN} u {@code OPERADOR}).
 */
public record LoginResponse(String token, String nombreCompleto, String nombre, String rol) {
}
