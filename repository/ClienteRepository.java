package com.example.backendprojt.repository;

import com.example.backendprojt.entity.Cliente;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de clientes.
 *
 * <p>El teléfono es la clave natural usada para reutilizar clientes en pedidos sucesivos
 * (RN-07).</p>
 */
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    /**
     * Busca un cliente por teléfono.
     *
     * @param telefono teléfono normalizado.
     * @return cliente encontrado, o vacío si no existe.
     */
    Optional<Cliente> findByTelefono(String telefono);
}
