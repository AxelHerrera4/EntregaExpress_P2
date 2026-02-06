package ec.edu.espe.trackingservice.service;

import ec.edu.espe.trackingservice.config.RabbitConfig;
import ec.edu.espe.trackingservice.dto.UbicacionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingProducer {

    private final RabbitTemplate rabbitTemplate;

    public void enviarUbicacion(UbicacionDTO dto) {
        log.info("Enviando ubicación a RabbitMQ: {}", dto);
        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE_NAME,
                RabbitConfig.ROUTING_KEY,
                dto
        );
        log.info("Ubicación enviada exitosamente");
    }
}

