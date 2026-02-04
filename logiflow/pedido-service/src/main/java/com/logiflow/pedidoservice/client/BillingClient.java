package com.logiflow.pedidoservice.client;

import com.logiflow.pedidoservice.dto.FacturaRequest;
import com.logiflow.pedidoservice.dto.FacturaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Cliente REST para comunicación con Billing Service
 * Usa RestTemplate (Spring tradicional)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BillingClient {

    private final RestTemplate restTemplate;

    @Value("${services.billing.url:http://localhost:8082}")
    private String billingServiceUrl;

    /**
     * Crea una factura en el Billing Service
     *
     * @param request datos del pedido para calcular la tarifa
     * @return respuesta con la factura creada
     */
    public FacturaResponse crearFactura(FacturaRequest request) {
        try {
            log.info("Llamando a Billing Service para crear factura - pedidoId: {}", request.getPedidoId());

            String url = billingServiceUrl + "/api/facturas";

            FacturaResponse response = restTemplate.postForObject(
                    url,
                    request,
                    FacturaResponse.class
            );

            log.info("Factura creada exitosamente - facturaId: {}, monto: {}",
                    response.getId(), response.getMontoTotal());

            return response;

        } catch (RestClientException e) {
            log.error("Error al comunicarse con Billing Service: {}", e.getMessage());
            throw new RuntimeException("No se pudo crear la factura: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene una factura por ID del pedido
     *
     * @param pedidoId ID del pedido (UUID como String)
     * @return respuesta con la factura
     */
    public FacturaResponse obtenerFacturaPorPedidoId(String pedidoId) {
        try {
            log.info("Consultando factura por pedidoId: {}", pedidoId);

            String url = billingServiceUrl + "/api/facturas/pedido/" + pedidoId;

            FacturaResponse response = restTemplate.getForObject(url, FacturaResponse.class);

            log.info("Factura encontrada - facturaId: {}", response.getId());

            return response;

        } catch (RestClientException e) {
            log.error("Error al obtener factura por pedidoId: {}", e.getMessage());
            return null; // Devuelve null si no se encuentra
        }
    }
}

