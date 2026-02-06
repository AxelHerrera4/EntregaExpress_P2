package com.logiflow.fleetservice.service.messaging;

import com.logiflow.fleetservice.event.TrackingUbicacionEvent;
import com.logiflow.fleetservice.model.entity.repartidor.Repartidor;
import com.logiflow.fleetservice.model.entity.vehiculo.Coordenada;
import com.logiflow.fleetservice.repository.RepartidorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Listener para eventos de ubicación desde TrackingService
 * Mantiene actualizada la ubicación de repartidores en FleetService
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TrackingEventListener {

    private final RepartidorRepository repartidorRepository;

    /**
     * Consume eventos de ubicación publicados por TrackingService
     * Actualiza la ubicación del repartidor en el contexto de FleetService
     */
    @RabbitListener(queues = "${rabbitmq.queue.tracking-ubicacion}")
    @Transactional
    public void handleUbicacionActualizada(TrackingUbicacionEvent event) {
        try {
            log.info("Evento de ubicación recibido de TrackingService - Repartidor ID: {}, Lat: {}, Lon: {}", 
                    event.getRepartidorId(), event.getLatitud(), event.getLongitud());
            
            // Convertir Long a UUID (asumiendo que repartidorId es Long en TrackingService)
            // Necesitamos buscar el repartidor de otra manera si los IDs no coinciden
            
            // Buscar repartidor por algún criterio
            // Por ahora logeamos el evento y marcamos como TODO la implementación
            log.info("Procesando actualización de ubicación para repartidor {}", event.getRepartidorId());
            
            // TODO: Implementar búsqueda de repartidor
            // Opción 1: Si existe mapeo entre Long (tracking) y UUID (fleet)
            // Opción 2: Buscar por documento u otro campo único
            // Opción 3: Mantener tabla de mapeo entre IDs
            
            log.debug("Ubicación recibida: [{}, {}] en timestamp: {}", 
                    event.getLatitud(), event.getLongitud(), event.getTimestamp());
            
        } catch (Exception e) {
            log.error("Error al procesar evento de ubicación desde TrackingService: {}", 
                    e.getMessage(), e);
            // No relanzamos la excepción para evitar reenvíos innecesarios
            // En producción, considerar enviar a DLQ
        }
    }

    /**
     * Actualiza la ubicación de un repartidor dado su UUID
     */
    private void actualizarUbicacionRepartidor(UUID repartidorId, Double latitud, Double longitud) {
        repartidorRepository.findById(repartidorId).ifPresentOrElse(
            repartidor -> {
                Coordenada nuevaUbicacion = new Coordenada(latitud, longitud);
                repartidor.setUbicacionActual(nuevaUbicacion);
                repartidorRepository.save(repartidor);
                log.info("Ubicación actualizada exitosamente para repartidor: {}", repartidorId);
            },
            () -> log.warn("Repartidor no encontrado con ID: {}", repartidorId)
        );
    }
}
