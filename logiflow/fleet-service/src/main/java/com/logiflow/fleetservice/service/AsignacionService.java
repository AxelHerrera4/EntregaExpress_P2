package com.logiflow.fleetservice.service;

import com.logiflow.fleetservice.dto.request.AsignacionRequest;
import com.logiflow.fleetservice.dto.response.AsignacionResponse;
import com.logiflow.fleetservice.model.entity.repartidor.Repartidor;
import com.logiflow.fleetservice.model.entity.vehiculo.VehiculoEntrega;
import com.logiflow.fleetservice.model.entity.enums.EstadoRepartidor;
import com.logiflow.fleetservice.repository.RepartidorRepository;
import com.logiflow.fleetservice.repository.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para asignar repartidores y vehículos a pedidos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsignacionService {

    private final RepartidorRepository repartidorRepository;
    private final VehiculoRepository vehiculoRepository;

    /**
     * Asigna un repartidor y vehículo disponible a un pedido
     * Algoritmo de asignación:
     * 1. Busca repartidores disponibles en la zona
     * 2. Selecciona el que tenga vehículo asignado y disponible
     * 3. Prioriza por calificación y experiencia
     */
    @Transactional
    public AsignacionResponse asignarRepartidorYVehiculo(AsignacionRequest request) {
        log.info("Iniciando asignación para pedido: {}", request.getPedidoId());

        // Buscar repartidores disponibles
        List<Repartidor> repartidoresDisponibles = repartidorRepository
                .findByEstadoAndActivoTrue(EstadoRepartidor.DISPONIBLE);

        if (repartidoresDisponibles.isEmpty()) {
            log.warn("No hay repartidores disponibles");
            return AsignacionResponse.builder()
                    .pedidoId(request.getPedidoId())
                    .estado("RECHAZADO")
                    .mensaje("No hay repartidores disponibles en este momento")
                    .build();
        }

        // Buscar el mejor repartidor con vehículo disponible
        Optional<Repartidor> repartidorSeleccionado = seleccionarMejorRepartidor(
                repartidoresDisponibles,
                request.getPeso()
        );

        if (repartidorSeleccionado.isEmpty()) {
            log.warn("No se encontró repartidor adecuado con vehículo disponible");
            return AsignacionResponse.builder()
                    .pedidoId(request.getPedidoId())
                    .estado("RECHAZADO")
                    .mensaje("No hay repartidores con vehículos adecuados disponibles")
                    .build();
        }

        Repartidor repartidor = repartidorSeleccionado.get();
        VehiculoEntrega vehiculo = repartidor.getVehiculoAsignado();
        // Cambiar estado del repartidor a EN_RUTA
        repartidor.setEstado(EstadoRepartidor.EN_RUTA);
        repartidorRepository.save(repartidor);

        log.info("Asignación exitosa - Repartidor: {} ({}), Vehículo: {} ({})",
                repartidor.getId(), repartidor.getNombreCompleto(),
                vehiculo.getId(), vehiculo.getPlaca());

        return AsignacionResponse.builder()
                .pedidoId(request.getPedidoId())
                .repartidorId(String.valueOf(repartidor.getId()))
                .vehiculoId(String.valueOf(vehiculo.getId()))
                .repartidorNombre(repartidor.getNombreCompleto())
                .vehiculoPlaca(vehiculo.getPlaca())
                .estado("ASIGNADO")
                .mensaje("Repartidor y vehículo asignados exitosamente")
                .build();
    }

    /**
     * Libera la asignación de un repartidor cuando un pedido es cancelado
     */
    @Transactional
    public void liberarAsignacion(String pedidoId) {
        log.info("Liberando asignación para pedido: {}", pedidoId);

        // Buscar repartidor EN_RUTA y marcarlo como DISPONIBLE
        // Nota: En una implementación real, deberías mantener un registro
        // de asignaciones pedido-repartidor
        List<Repartidor> repartidoresEnRuta = repartidorRepository
                .findByEstadoAndActivoTrue(EstadoRepartidor.EN_RUTA);

        // Por simplicidad, liberamos el primer repartidor encontrado
        // En producción deberías tener una tabla de asignaciones
        if (!repartidoresEnRuta.isEmpty()) {
            Repartidor repartidor = repartidoresEnRuta.get(0);
            repartidor.setEstado(EstadoRepartidor.DISPONIBLE);
            repartidorRepository.save(repartidor);
            log.info("Repartidor {} liberado", repartidor.getId());
        }
    }

    /**
     * Selecciona el mejor repartidor disponible
     * NOTA: Esta funcionalidad está fuera del scope de la Fase 1 según documentación
     * Requiere métricas y calificaciones no implementadas en el modelo actual
     */
    private Optional<Repartidor> seleccionarMejorRepartidor(
            List<Repartidor> repartidores,
            Double pesoRequerido
    ) {
        // Implementación simplificada temporal
        return repartidores.stream()
                // Filtrar: debe tener vehículo asignado y activo
                .filter(r -> r.getVehiculoAsignado() != null)
                .filter(r -> r.getVehiculoAsignado().getEstado() == com.logiflow.fleetservice.model.entity.enums.EstadoVehiculo.ACTIVO)
                // Filtrar: el vehículo debe soportar el peso
                .filter(r -> r.getVehiculoAsignado().getCapacidadCarga() >= pesoRequerido)
                // Retornar el primero disponible (sin métricas de calificación)
                .findFirst();
    }
}

