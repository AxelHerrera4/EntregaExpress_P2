package com.logiflow.pedidoservice.client;

import com.logiflow.pedidoservice.dto.FacturaRequest;
import com.logiflow.pedidoservice.dto.FacturaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
     * @param token
     * @return respuesta con la factura creada
     */


        public FacturaResponse crearFactura(FacturaRequest request, String token) {
            try {
                log.info("Llamando a Billing Service para crear factura - pedidoId: {}", request.getPedidoId());

                String url = billingServiceUrl + "/api/facturas";

                // 🔹 CONFIGURACIÓN DE HEADERS
                HttpHeaders headers = new HttpHeaders();

                // Verificamos si el token ya trae la palabra "Bearer "
                String tokenHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;
                headers.set("Authorization", tokenHeader);

                // 🔹 Envolvemos request + headers
                HttpEntity<FacturaRequest> entity = new HttpEntity<>(request, headers);

                // 🔹 Hacemos el POST (Cambiamos postForObject por exchange para mayor control si fuera necesario)
                FacturaResponse response = restTemplate.postForObject(
                        url,
                        entity,
                        FacturaResponse.class
                );

                log.info("✅ Factura creada exitosamente - facturaId: {}", response.getId());
                return response;

            } catch (RestClientException e) {
                log.error("❌ Error al comunicarse con Billing Service: {}", e.getMessage());
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

