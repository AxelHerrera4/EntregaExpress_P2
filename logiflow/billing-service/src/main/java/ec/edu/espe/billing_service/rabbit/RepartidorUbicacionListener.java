package ec.edu.espe.billing_service.rabbit;

import ec.edu.espe.billing_service.event.RepartidorUbicacionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RepartidorUbicacionListener {

    private final Set<String> processedMessages = new HashSet<>();

    @RabbitListener(queues = "${rabbitmq.queue.repartidor-ubicacion}")
    public void escucharUbicacionRepartidor(RepartidorUbicacionEvent event) {
        
        log.info("=====================================================");
        log.info("📍 [RABBIT-CONSUMER] Evento UBICACION REPARTIDOR recibido");
        log.info("🆔  Message ID     : {}", event.getMessageId());
        log.info("⏰  Timestamp      : {}", event.getTimestamp());
        log.info("👤  Repartidor ID  : {}", event.getRepartidorId());
        log.info("📦  Pedido ID      : {}", event.getPedidoId());
        log.info("🚚  Vehículo ID    : {}", event.getVehiculoId());
        log.info("🗺️   Ubicación      : (Lat: {}, Lng: {})", event.getLatitud(), event.getLongitud());
        log.info("📍  Dirección      : {}", event.getDireccion());
        log.info("⚡  Estado         : {}", event.getEstadoRepartidor());
        log.info("🏃  Velocidad      : {} km/h", event.getVelocidadKmh());
        log.info("🔋  Batería        : {}%", event.getBateriaPorcentaje());
        log.info("🛣️   En Ruta        : {}", event.getEnRuta());
        log.info("=====================================================");

        // Control de idempotencia
        if (processedMessages.contains(event.getMessageId())) {
            log.warn("⚠️  [IDEMPOTENCIA] Mensaje ya procesado, ignorando | MessageID: {} | RepartidorID: {} | PedidoID: {}", 
                event.getMessageId(), event.getRepartidorId(), event.getPedidoId());
            return;
        }

        try {
            log.info("🏁 [BILLING-TRACKING] Procesando información de ubicación para cobros/kilometraje | PedidoID: {} | RepartidorID: {} | MessageID: {}", 
                event.getPedidoId(), event.getRepartidorId(), event.getMessageId());
                
            // Aquí se puede agregar lógica para:
            // - Calcular kilómetros recorridos para facturación
            // - Actualizar tarifas dinámicas basadas en ubicación
            // - Registrar tiempo de entrega para análisis de costos
            // - Detectar desvíos de ruta que afecten el costo
            
            processedMessages.add(event.getMessageId());
            
            log.info("✅ [BILLING-SUCCESS] Ubicación del repartidor procesada exitosamente | PedidoID: {} | RepartidorID: {} | MessageID: {}", 
                event.getPedidoId(), event.getRepartidorId(), event.getMessageId());
            log.info("🔗 [CORRELACION-BILLING] MessageID={} | PedidoID={} | RepartidorID={} | Ubicacion=({}:{})", 
                event.getMessageId(), event.getPedidoId(), event.getRepartidorId(), event.getLatitud(), event.getLongitud());
            
        } catch (Exception e) {
            log.error("❌ [BILLING-ERROR] Error procesando ubicación de repartidor | PedidoID={} | RepartidorID={} | MessageID={} | Error={}", 
                    event.getPedidoId(), event.getRepartidorId(), event.getMessageId(), e.getMessage(), e);
            throw e; // Relanzar para que RabbitMQ maneje el retry
        }
    }
}