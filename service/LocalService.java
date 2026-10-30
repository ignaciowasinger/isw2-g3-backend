package com.example.backendprojt.service;

import com.example.backendprojt.dto.LocalRequest;
import com.example.backendprojt.dto.LocalResponse;
import com.example.backendprojt.entity.Local;
import com.example.backendprojt.exception.ConflictoException;
import com.example.backendprojt.exception.RecursoNoEncontradoException;
import com.example.backendprojt.repository.LocalRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de locales (RF-03).
 *
 * <p>Expone listados públicos (solo activos), listados administrativos (todos) y el CRUD
 * lógico. El nombre es único sin distinguir mayúsculas y la baja se implementa con la
 * bandera {@code activo}, sin eliminar filas (RN-10).</p>
 */
@Service
public class LocalService {

    private final LocalRepository localRepository;

    /**
     * Construye el servicio con el repositorio de locales.
     *
     * @param localRepository repositorio de locales.
     */
    public LocalService(LocalRepository localRepository) {
        this.localRepository = localRepository;
    }

    /**
     * Lista los locales activos para el catálogo público.
     *
     * @return locales activos ordenados por nombre.
     */
    @Transactional(readOnly = true)
    public List<LocalResponse> listarActivos() {
        return localRepository.findByActivoTrueOrderByNombreAsc().stream().map(this::aDto).toList();
    }

    /**
     * Obtiene un local activo para el catálogo público.
     *
     * @param id identificador del local.
     * @return local activo.
     * @throws RecursoNoEncontradoException si no existe o está inactivo.
     */
    @Transactional(readOnly = true)
    public LocalResponse obtenerActivo(Integer id) {
        return aDto(buscarActivo(id));
    }

    /**
     * Busca un local y exige que esté activo.
     *
     * @param id identificador del local.
     * @return entidad local activa.
     * @throws RecursoNoEncontradoException si no existe o está inactivo.
     */
    @Transactional(readOnly = true)
    public Local buscarActivo(Integer id) {
        Local local = buscar(id);
        if (!local.isActivo()) {
            throw new RecursoNoEncontradoException("Local no encontrado: " + id);
        }
        return local;
    }

    /**
     * Lista todos los locales para el panel administrativo.
     *
     * @return locales activos e inactivos ordenados por nombre.
     */
    @Transactional(readOnly = true)
    public List<LocalResponse> listarTodos() {
        return localRepository.findAll(Sort.by("nombre")).stream().map(this::aDto).toList();
    }

    /**
     * Obtiene un local para el panel administrativo, sin filtrar por estado.
     *
     * @param id identificador del local.
     * @return local solicitado.
     * @throws RecursoNoEncontradoException si no existe.
     */
    @Transactional(readOnly = true)
    public LocalResponse obtener(Integer id) {
        return aDto(buscar(id));
    }

    /**
     * Crea un local con nombre único.
     *
     * @param req datos validados del local.
     * @return local creado.
     * @throws ConflictoException si ya existe un local con el mismo nombre.
     */
    @Transactional
    public LocalResponse crear(LocalRequest req) {
        String nombre = req.nombre().trim();
        if (localRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ConflictoException("Ya existe un local con el nombre '" + nombre + "'");
        }
        Local local = new Local();
        local.setNombre(nombre);
        local.setDireccion(limpiar(req.direccion()));
        local.setTelefono(limpiar(req.telefono()));
        local.setActivo(true);
        return aDto(localRepository.save(local));
    }

    /**
     * Actualiza los datos de un local.
     *
     * <p>El control de duplicados excluye al propio local. Si {@code activo} es
     * {@code null}, se conserva el estado actual (RN-10).</p>
     *
     * @param id  identificador del local.
     * @param req datos validados del local.
     * @return local actualizado.
     * @throws RecursoNoEncontradoException si el local no existe.
     * @throws ConflictoException           si el nombre choca con otro local.
     */
    @Transactional
    public LocalResponse actualizar(Integer id, LocalRequest req) {
        Local local = buscar(id);
        String nombre = req.nombre().trim();
        if (localRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ConflictoException("Ya existe un local con el nombre '" + nombre + "'");
        }
        local.setNombre(nombre);
        local.setDireccion(limpiar(req.direccion()));
        local.setTelefono(limpiar(req.telefono()));
        if (req.activo() != null) {
            local.setActivo(req.activo());
        }
        return aDto(localRepository.save(local));
    }

    /**
     * Aplica una baja lógica al local (RN-10); no elimina la fila.
     *
     * @param id identificador del local.
     * @throws RecursoNoEncontradoException si el local no existe.
     */
    @Transactional
    public void desactivar(Integer id) {
        Local local = buscar(id);
        local.setActivo(false);
        localRepository.save(local);
    }

    /**
     * Busca un local por identificador sin filtrar por estado.
     *
     * @param id identificador del local.
     * @return entidad local.
     * @throws RecursoNoEncontradoException si no existe.
     */
    private Local buscar(Integer id) {
        return localRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Local no encontrado: " + id));
    }

    /**
     * Normaliza textos opcionales: recorta espacios y convierte vacíos en {@code null}.
     *
     * @param valor texto original.
     * @return texto normalizado o {@code null}.
     */
    private String limpiar(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    /**
     * Convierte la entidad al DTO de respuesta.
     *
     * @param l entidad local.
     * @return DTO de local.
     */
    private LocalResponse aDto(Local l) {
        return new LocalResponse(l.getId(), l.getNombre(), l.getDireccion(), l.getTelefono(), l.isActivo());
    }
}
