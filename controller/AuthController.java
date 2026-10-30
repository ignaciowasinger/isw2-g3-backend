package com.example.backendprojt.controller;

import com.example.backendprojt.dto.LoginRequest;
import com.example.backendprojt.dto.LoginResponse;
import com.example.backendprojt.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de autenticación del personal (RF-01).
 *
 * <p>Único endpoint público de seguridad. Verifica credenciales con BCrypt y devuelve un
 * JWT firmado con vigencia de ocho horas.</p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Construye el controlador con el servicio de autenticación.
     *
     * @param authService servicio de login.
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Inicia sesión y emite un token JWT.
     *
     * <p><b>POST {@code /api/auth/login}</b> · público.</p>
     *
     * @param request credenciales del usuario.
     * @return token, nombre y rol.
     * @throws org.springframework.security.core.AuthenticationException si las credenciales
     *                                                                   son inválidas o el usuario está inactivo.
     * @response 200 token emitido.
     * @response 400 cuerpo o campos inválidos.
     * @response 401 credenciales incorrectas.
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
