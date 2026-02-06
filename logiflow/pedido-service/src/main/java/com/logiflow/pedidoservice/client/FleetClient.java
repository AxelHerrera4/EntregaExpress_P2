package com.logiflow.pedidoservice.client;

import com.logiflow.pedidoservice.dto.AsignacionRequest;
import com.logiflow.pedidoservice.dto.AsignacionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Cliente REST para comunicación con Fleet Service
 * Usa RestTemplate (Spring tradicional)
 * NOTA: Este servicio aún no está implementado, preparado para uso futuro
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FleetClient {

    private final RestTemplate restTemplate;

    @Value("${services.fleet.url:http://localhost:8083}")
    private String fleetServiceUrl;

    /**
     * Solicita asignación de repartidor y vehículo al Fleet Service
     *
     * @param request datos del pedido para la asignación
     * @param token
     * @return respuesta con repartidor y vehículo asignados
     */
    public AsignacionResponse asignarRepartidor(AsignacionRequest request, String token) {
        try {
            log.info("Llamando a Fleet Service para asignar repartidor - pedidoId: {}", request.getPedidoId());

            String url = fleetServiceUrl + "/api/asignaciones";

            AsignacionResponse response = restTemplate.postForObject(
                    url,
                    request,
                    AsignacionResponse.class
            );

            if (response != null) {
                log.info("Repartidor asignado exitosamente - repartidorId: {}, vehiculoId: {}",
                        response.getRepartidorId(), response.getVehiculoId());
            }

            return response;

        } catch (RestClientException e) {
            log.error("Error al comunicarse con Fleet Service: {}", e.getMessage());
            // Retornar respuesta con estado RECHAZADO en lugar de lanzar excepción
            return AsignacionResponse.builder()
                    .estado("RECHAZADO")
                    .mensaje("Fleet Service no disponible: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Libera la asignación de un repartidor cuando un pedido es cancelado
     * @param pedidoId ID del pedido a cancelar
     */
    public void liberarAsignacion(String pedidoId) {
        try {
            log.info("Liberando asignación para pedido: {}", pedidoId);

            String url = fleetServiceUrl + "/api/asignaciones/pedido/" + pedidoId + "/liberar";

            restTemplate.delete(url);

            log.info("Asignación liberada exitosamente");

        } catch (RestClientException e) {
            log.error("Error al liberar asignación en Fleet Service: {}", e.getMessage());
            // No lanzamos excepción para no afectar la cancelación del pedido
        }
    }
}

