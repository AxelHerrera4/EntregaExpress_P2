package logiflow.ms_notifications.client;

import logiflow.ms_notifications.dto.PedidoResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;

/**
 * Cliente HTTP para consumir pedido-service
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PedidoServiceClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${pedido.service.url:http://pedido-service:8084/api/pedidos}")
    private String pedidoServiceUrl;
    
    /**
     * Obtiene los detalles de un pedido por ID
     */
    public Optional<PedidoResponseDto> obtenerPedido(String pedidoId) {
        try {
            String url = pedidoServiceUrl + "/" + pedidoId;
            log.info("Consultando pedido desde: {}", url);
            
            PedidoResponseDto response = restTemplate.getForObject(url, PedidoResponseDto.class);
            return Optional.ofNullable(response);
            
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("Pedido no encontrado: {}", pedidoId);
                return Optional.empty();
            }
            log.error("Error al consultar pedido: {}", pedidoId, e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error al conectar con pedido-service para pedido: {}", pedidoId, e);
            return Optional.empty();
        }
    }
}
