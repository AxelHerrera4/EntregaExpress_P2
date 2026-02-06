package com.logiflow.pedidoservice.rabbit;

import com.logiflow.pedidoservice.event.PedidoCreadoEvent;
import com.logiflow.pedidoservice.event.PedidoEstadoEvent;
import com.logiflow.pedidoservice.event.ReintentarAsignacionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PedidoEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.pedidos:pedidos.exchange}")
    private String pedidosExchange;

    @Value("${rabbitmq.routing-key.pedido-creado:pedido.creado}")
    private String pedidoCreadoRoutingKey;

    @Value("${rabbitmq.routing-key.pedido-estado:pedido.estado.actualizado}")
    private String pedidoEstadoRoutingKey;

    @Value("${rabbitmq.routing-key.reintento-asignacion:pedido.reintento.asignacion}")
    private String reintentoAsignacionRoutingKey;

    public PedidoEventPublisher(@Lazy RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishPedidoCreadoEvent(PedidoCreadoEvent event) {
        log.info("=====================================================");
        log.info("[RABBIT-PRODUCER] Publicando evento PEDIDO CREADO");
        log.info("Message ID     : {}", event.getMessageId());
        log.info("Timestamp      : {}", event.getTimestamp());
        log.info("Pedido ID      : {}", event.getPedidoId());
        log.info("Cliente        : {}", event.getClienteId());
        log.info("Usuario Creador: {}", event.getUsuarioCreador());
        log.info("Estado         : {}", event.getEstado());
        log.info("Tipo Entrega   : {}", event.getTipoEntrega());
        log.info("Modalidad      : {}", event.getModalidadServicio());
        log.info("Origen         : {} ({})", event.getDireccionOrigen(), event.getCiudadOrigen());
        log.info("Destino        : {} ({})", event.getDireccionDestino(), event.getCiudadDestino());
        log.info("Distancia      : {} km", event.getDistanciaEstimadaKm());
        log.info("Tarifa         : {}", event.getTarifaCalculada());
        log.info("Exchange       : {}", pedidosExchange);
        log.info("RoutingKey     : {}", pedidoCreadoRoutingKey);
        log.info("=====================================================");

        try {
            rabbitTemplate.convertAndSend(pedidosExchange, pedidoCreadoRoutingKey, event);
            log.info("[RABBIT-PRODUCER] Evento pedido.creado enviado EXITOSAMENTE");
            log.info("[CORRELACION] MessageID={} | PedidoID={} | Usuario={}", 
                event.getMessageId(), event.getPedidoId(), event.getUsuarioCreador());
        } catch (Exception e) {
            log.error("[RABBIT-PRODUCER] ERROR enviando evento pedido.creado para pedidoId={}",
                    event.getPedidoId(), e);
            log.error("[ERROR-DETAILS] MessageID={} | Usuario={} | Error={}", 
                event.getMessageId(), event.getUsuarioCreador(), e.getMessage());
            // NO relanzamos la excepción para evitar rollback de la transacción DB
            // El pedido se guarda aunque el evento falle (se puede reintentar después)
        }
    }

    public void publishPedidoEstadoEvent(PedidoEstadoEvent event) {
        log.info("=====================================================");
        log.info("[RABBIT-PRODUCER] Publicando evento ESTADO ACTUALIZADO");
        log.info("Message ID     : {}", event.getMessageId());
        log.info("Timestamp      : {}", event.getTimestamp());
        log.info("Pedido ID      : {}", event.getPedidoId());
        log.info("Estado Anterior: {}", event.getEstadoAnterior());
        log.info("Estado Nuevo   : {}", event.getEstadoNuevo());
        log.info("Usuario        : {}", event.getUsuarioModificador());
        log.info("Repartidor     : {}", event.getRepartidorId());
        log.info("Vehículo       : {}", event.getVehiculoId());
        log.info("Exchange       : {}", pedidosExchange);
        log.info("RoutingKey     : {}", pedidoEstadoRoutingKey);
        log.info("=====================================================");

        try {
            rabbitTemplate.convertAndSend(pedidosExchange, pedidoEstadoRoutingKey, event);
            log.info("[RABBIT-PRODUCER] Evento pedido.estado.actualizado enviado EXITOSAMENTE");
            log.info("[CORRELACION] MessageID={} | PedidoID={} | {}\u2192{} | Usuario={}", 
                event.getMessageId(), event.getPedidoId(), event.getEstadoAnterior(), 
                event.getEstadoNuevo(), event.getUsuarioModificador());
        } catch (Exception e) {
            log.error("[RABBIT-PRODUCER] ERROR enviando evento pedido.estado.actualizado para pedidoId={}",
                    event.getPedidoId(), e);
            log.error("[ERROR-DETAILS] MessageID={} | Usuario={} | {}\u2192{} | Error={}", 
                event.getMessageId(), event.getUsuarioModificador(), event.getEstadoAnterior(), 
                event.getEstadoNuevo(), e.getMessage());
            // NO relanzamos la excepción para evitar rollback de la transacción DB
            // El pedido se guarda aunque el evento falle (se puede reintentar después)
        }
    }

    public void publishReintentarAsignacionEvent(ReintentarAsignacionEvent event) {
        log.info("=====================================================");
        log.info("[RABBIT-PRODUCER] Publicando evento REINTENTAR ASIGNACION");
        log.info("Message ID     : {}", event.getMessageId());
        log.info("Timestamp      : {}", event.getTimestamp());
        log.info("Pedido ID      : {}", event.getPedidoId());
        log.info("Cliente        : {}", event.getClienteId());
        log.info("Usuario        : {}", event.getUsuarioSolicitante());
        log.info("Tipo Entrega   : {}", event.getTipoEntrega());
        log.info("Modalidad      : {}", event.getModalidadServicio());
        log.info("Prioridad      : {}", event.getPrioridad());
        log.info("Origen         : {}", event.getCiudadOrigen());
        log.info("Destino        : {}", event.getCiudadDestino());
        log.info("Reintento #    : {}", event.getNumeroReintento());
        log.info("Motivo         : {}", event.getMotivoReintento());
        log.info("Exchange       : {}", pedidosExchange);
        log.info("RoutingKey     : {}", reintentoAsignacionRoutingKey);
        log.info("=====================================================");

        try {
            rabbitTemplate.convertAndSend(pedidosExchange, reintentoAsignacionRoutingKey, event);
            log.info("[RABBIT-PRODUCER] Evento pedido.reintento.asignacion enviado EXITOSAMENTE");
            log.info("[CORRELACION] MessageID={} | PedidoID={} | Reintento#{} | Motivo={}", 
                event.getMessageId(), event.getPedidoId(), event.getNumeroReintento(), event.getMotivoReintento());
        } catch (Exception e) {
            log.error("[RABBIT-PRODUCER] ERROR enviando evento pedido.reintento.asignacion para pedidoId={}",
                    event.getPedidoId(), e);
            log.error("[ERROR-DETAILS] MessageID={} | Usuario={} | Error={}", 
                event.getMessageId(), event.getUsuarioSolicitante(), e.getMessage());
        }
    }
}