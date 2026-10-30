package com.example.backendprojt.service;

import com.example.backendprojt.dto.RepartidorResponse;
import com.example.backendprojt.repository.RepartidorRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de consulta de repartidores.
 *
 * <p>Devuelve el padrón completo (activos e inactivos) al personal autorizado. La vista
 * de repartidores en calle, que excluye inactivos e incluye a quienes no tienen pedidos,
 * se resuelve en {@link GestionPedidoService#enCalle()} (RF-12).</p>
 */
@Service
public class RepartidorService {

    private final RepartidorRepository repartidorRepository;

    /**
     * Construye el servicio con el repositorio de repartidores.
     *
     * @param repartidorRepository repositorio de repartidores.
     */
    public RepartidorService(RepartidorRepository repartidorRepository) {
        this.repartidorRepository = repartidorRepository;
    }

    /**
     * Lista todos los repartidores.
     *
     * @return repartidores ordenados por identificador.
     */
    @Transactional(readOnly = true)
    public List<RepartidorResponse> listar() {
        return repartidorRepository.findAll(Sort.by("id")).stream()
                .map(r -> new RepartidorResponse(r.getId(), r.getNombreCompleto(), r.getTelefono(), r.isActivo()))
                .toList();
    }
}
