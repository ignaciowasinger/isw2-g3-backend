package com.example.backendprojt.service;

import com.example.backendprojt.dto.LoginRequest;
import com.example.backendprojt.dto.LoginResponse;
import com.example.backendprojt.security.JwtService;
import com.example.backendprojt.security.UsuarioPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Servicio de autenticación del personal (RF-01, RNF-05).
 *
 * <p>Delega la verificación de credenciales en {@link AuthenticationManager}, que aplica
 * BCrypt mediante el {@code PasswordEncoder} configurado, y emite un JWT firmado con
 * vigencia de ocho horas.</p>
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Construye el servicio con sus dependencias.
     *
     * @param authenticationManager gestor de autenticación de Spring Security.
     * @param jwtService             servicio de emisión de tokens.
     */
    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Autentica un usuario y devuelve el token y sus datos públicos.
     *
     * <p>Las credenciales inválidas o el usuario inactivo producen
     * {@link org.springframework.security.core.AuthenticationException}, que el manejador
     * global traduce a HTTP 401 con el mensaje genérico
     * {@code "Usuario o contraseña incorrectos"}.</p>
     *
     * @param request credenciales validadas.
     * @return token JWT, nombre completo (con alias {@code nombre}) y rol.
     * @throws org.springframework.security.core.AuthenticationException si las
     *                                                                   credenciales no son válidas.
     */
    public LoginResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        UsuarioPrincipal usuario = (UsuarioPrincipal) auth.getPrincipal();
        return new LoginResponse(jwtService.generarToken(usuario),
                usuario.getNombreCompleto(), usuario.getNombreCompleto(), usuario.getRol().name());
    }
}
