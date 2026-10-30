package com.example.backendprojt.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro que autentica cada petición a partir del encabezado {@code Authorization}.
 *
 * <p>Si recibe {@code Bearer <token>}, valida la firma, recarga el usuario desde la base
 * para que una baja o un cambio de rol se apliquen de inmediato y, si está habilitado,
 * registra la autenticación en el contexto de seguridad. Ante cualquier token inválido
 * limpia el contexto y deja que la cadena de seguridad responda 401. No crea sesiones:
 * la aplicación es completamente stateless (RNF-05).</p>
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    /** Prefijo estándar del encabezado de autorización. */
    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;

    /**
     * Construye el filtro con sus dependencias.
     *
     * @param jwtService             servicio de validación de tokens.
     * @param usuarioDetailsService  servicio de carga de usuarios.
     */
    public JwtAuthFilter(JwtService jwtService, UsuarioDetailsService usuarioDetailsService) {
        this.jwtService = jwtService;
        this.usuarioDetailsService = usuarioDetailsService;
    }

    /**
     * Procesa la autenticación de la petición antes de continuar la cadena.
     *
     * @param request     petición HTTP.
     * @param response    respuesta HTTP.
     * @param filterChain cadena de filtros.
     * @throws ServletException si ocurre un error de servlet.
     * @throws IOException      si ocurre un error de entrada/salida.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String cabecera = request.getHeader("Authorization");
        if (cabecera != null && cabecera.startsWith(PREFIJO)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = jwtService.parsear(cabecera.substring(PREFIJO.length()));
                // Se relee el usuario para que una baja o un cambio de rol tengan efecto inmediato.
                UserDetails detalle = usuarioDetailsService.loadUserByUsername(claims.getSubject());
                if (detalle.isEnabled()) {
                    UsernamePasswordAuthenticationToken autenticacion =
                            new UsernamePasswordAuthenticationToken(detalle, null, detalle.getAuthorities());
                    autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(autenticacion);
                }
            } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
