package com.example.backendprojt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Punto de entrada de la API de Sabores del Litoral.
 *
 * <p>Habilita el auto-configurado de Spring Boot y el escaneo de propiedades tipadas
 * ({@code app.jwt.*}) usadas por la seguridad. La aplicación se ejecuta sobre H2 en modo
 * archivo y expone la API REST en el puerto configurado (8080 por defecto).</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class BackendprojtApplication {

    /**
     * Inicia la aplicación Spring Boot.
     *
     * @param args argumentos de línea de comandos.
     */
    public static void main(String[] args) {
        SpringApplication.run(BackendprojtApplication.class, args);
    }
}
