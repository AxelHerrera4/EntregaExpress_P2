package ec.edu.espe.billing_service.service.impl;

import ec.edu.espe.billing_service.model.dto.request.TarifaBaseRequestDTO;
import ec.edu.espe.billing_service.model.dto.response.TarifaBaseResponseDTO;
import ec.edu.espe.billing_service.model.entity.TarifaBase;
import ec.edu.espe.billing_service.repository.TarifaBaseRepository;
import ec.edu.espe.billing_service.service.TarifaBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TarifaBaseServiceImpl implements TarifaBaseService {

    private final TarifaBaseRepository tarifaBaseRepository;

    @Override
    public TarifaBaseResponseDTO crearTarifa(TarifaBaseRequestDTO request) {
        log.info("Creando tarifa base | tipoEntrega={} | tarifa={}",
                request.getTipoEntrega(),
                request.getTarifaBase());

        String tipoEntrega = normalizeTipoEntrega(request.getTipoEntrega());

        if (tarifaBaseRepository.existsByTipoEntrega(tipoEntrega)) {
            log.warn("Intento de crear tarifa duplicada | tipoEntrega={}", tipoEntrega);
            throw new RuntimeException(
                    "Ya existe una tarifa para el tipo de entrega: " + tipoEntrega
            );
        }

        TarifaBase tarifaBase = TarifaBase.builder()
                .tipoEntrega(tipoEntrega)
                .tarifaBase(request.getTarifaBase())
                .build();

        TarifaBase guardada = tarifaBaseRepository.save(tarifaBase);
        log.info("Tarifa base creada | id={} | tipoEntrega={}",
                guardada.getId(),
                guardada.getTipoEntrega());
        return mapToResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public TarifaBaseResponseDTO obtenerPorTipoEntrega(String tipoEntrega) {
        log.debug("Buscando tarifa base por tipoEntrega={}", tipoEntrega);

        final String tipo = tipoEntrega.toUpperCase(); // nueva variable final

        TarifaBase tarifa = tarifaBaseRepository.findByTipoEntrega(tipo)
                .orElseThrow(() -> {
                    log.error("No existe tarifa base | tipoEntrega={}", tipo);
                    return new RuntimeException(
                            "No existe tarifa para el tipo de entrega: " + tipo
                    );
                });

        return mapToResponse(tarifa);
    }

    @Override
    public TarifaBaseResponseDTO actualizarTarifa(String tipoEntrega, TarifaBaseRequestDTO request) {
        log.info("Actualizando tarifa base | tipoEntrega={} | nuevaTarifa={}",
                tipoEntrega,
                request.getTarifaBase());

        final String tipo = tipoEntrega.toUpperCase(); // nueva variable final

        TarifaBase tarifa = tarifaBaseRepository.findByTipoEntrega(tipo)
                .orElseThrow(() -> {
                    log.error("No existe tarifa base para actualizar | tipoEntrega={}", tipo);
                    return new RuntimeException(
                            "No existe tarifa para el tipo de entrega: " + tipo
                    );
                });


        tarifa.setTarifaBase(request.getTarifaBase());

        TarifaBase actualizada = tarifaBaseRepository.save(tarifa);
        log.info("Tarifa base actualizada | id={} | tipoEntrega={} | tarifa={}",
                actualizada.getId(),
                actualizada.getTipoEntrega(),
                actualizada.getTarifaBase());

        return mapToResponse(actualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public TarifaBase obtenerEntidadPorTipoEntrega(String tipoEntrega) {

        log.debug("Obteniendo entidad TarifaBase | tipoEntrega={}", tipoEntrega);
        final String tipo = normalizeTipoEntrega(tipoEntrega);

        return tarifaBaseRepository.findByTipoEntrega(tipo)
                .orElseThrow(() -> {
                    log.error("Entidad TarifaBase no encontrada | tipoEntrega={}", tipo);
                    return new RuntimeException(
                            "No existe tarifa para el tipo de entrega: " + tipo
                    );
                });

    }

    @Override
    @Transactional(readOnly = true)
    public List<TarifaBaseResponseDTO> obtenerTodasLasTarifas() {
        log.debug("Listando todas las tarifas base");
        log.info("Total de tarifas encontradas={}", tarifaBaseRepository.count());
        return tarifaBaseRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private TarifaBaseResponseDTO mapToResponse(TarifaBase tarifa) {
        return TarifaBaseResponseDTO.builder()
                .id(tarifa.getId())
                .tipoEntrega(tarifa.getTipoEntrega())
                .tarifaBase(tarifa.getTarifaBase())
                .build();
    }

    private String normalizeTipoEntrega(String tipoEntrega) {
        if (tipoEntrega == null || tipoEntrega.isBlank()) {
            log.error("TipoEntrega inválido (nulo o vacío)");
            throw new IllegalArgumentException("Tipo de entrega no puede ser nulo o vacío");
        }
        return tipoEntrega.toUpperCase();
    }

}

