package com.example.backendprojt.security;

import com.example.backendprojt.dto.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Configuración central de Spring Security (RF-01, RF-02, RNF-05 y RNF-06).
 *
 * <p>Define una cadena stateless basada en JWT, las reglas de acceso por rol y respuestas
 * de error uniformes en formato {@link ApiError}: 401 cuando falta o falla la
 * autenticación y 403 cuando el usuario autenticado no tiene permisos. También publica
 * los beans de codificación BCrypt, gestor de autenticación y CORS.</p>
 *
 * <p>Matriz de acceso:</p>
 * <ul>
 *   <li>Público: login, catálogo de locales/categorías/productos, alta de pedidos y
 *       seguimiento por código.</li>
 *   <li>ADMIN: usuarios y todo {@code /api/admin/**}, incluido el reporte de ventas.</li>
 *   <li>OPERADOR y ADMIN: consulta y operación de pedidos y repartidores.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final ObjectMapper objectMapper;

    /**
     * Construye la configuración con el filtro JWT y el serializador de errores.
     *
     * @param jwtAuthFilter filtro de autenticación JWT.
     * @param objectMapper  serializador usado para escribir {@link ApiError}.
     */
    public SecurityConfig(
            JwtAuthFilter jwtAuthFilter,
            ObjectMapper objectMapper) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.objectMapper = objectMapper;
    }

    /**
     * Bean de codificación de contraseñas.
     *
     * @return codificador BCrypt con la fuerza por defecto.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Expone el gestor de autenticación usado por {@code AuthService}.
     *
     * @param config configuración de autenticación de Spring.
     * @return gestor de autenticación.
     * @throws Exception si Spring no puede construir el gestor.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Define la cadena de filtros, las reglas de autorización y los manejadores de error.
     *
     * @param http constructor de la seguridad HTTP.
     * @return cadena de filtros configurada.
     * @throws Exception si la configuración no puede construirse.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, ex) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

                            objectMapper.writeValue(
                                    response.getOutputStream(),
                                    ApiError.de(
                                            HttpStatus.UNAUTHORIZED,
                                            "Se requiere autenticación",
                                            request.getRequestURI(),
                                            List.of()
                                    )
                            );
                        })
                        .accessDeniedHandler((request, response, ex) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

                            objectMapper.writeValue(
                                    response.getOutputStream(),
                                    ApiError.de(
                                            HttpStatus.FORBIDDEN,
                                            "No tenés permisos para realizar esta operación",
                                            request.getRequestURI(),
                                            List.of()
                                    )
                            );
                        }))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/locales/**", "/api/categorias/**",
                                "/api/productos/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/pedidos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/seguimiento/*").permitAll()
                        .requestMatchers("/api/admin/**", "/api/usuarios/**", "/api/ventas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/**", "/api/repartidores/**")
                                .hasAnyRole("OPERADOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/pedidos/*/avanzar", "/api/pedidos/*/cancelar")
                                .hasAnyRole("OPERADOR", "ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Configura CORS para permitir el consumo desde la app móvil y Expo durante el
     * desarrollo.
     *
     * @return fuente de configuración CORS para todas las rutas.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
