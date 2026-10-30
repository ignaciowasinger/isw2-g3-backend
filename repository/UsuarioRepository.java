package com.example.backendprojt.repository;

import com.example.backendprojt.entity.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de usuarios del sistema.
 *
 * <p>Provee las búsquedas usadas por autenticación y el control de unicidad del nombre de
 * usuario en el alta administrativa.</p>
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    /**
     * Busca un usuario por su nombre de acceso.
     *
     * @param username nombre de usuario.
     * @return usuario encontrado, o vacío si no existe.
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Comprueba si un nombre de usuario ya está registrado.
     *
     * @param username nombre de usuario.
     * @return {@code true} si existe.
     */
    boolean existsByUsername(String username);
}
