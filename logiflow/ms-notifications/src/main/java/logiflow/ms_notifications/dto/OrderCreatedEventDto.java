package logiflow.ms_notifications.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEventDto {

    // Para idempotencia
    private String messageId;
    private LocalDateTime timestamp;

    // Información del pedido
    private String pedidoId;
    private String clienteId;
    private String usuarioCreador; // Usuario que creó el pedido
    private String estado;
    private String tipoEntrega;
    private String modalidadServicio;
    private String prioridad;
    private Double peso;
    private String direccionOrigen;
    private String direccionDestino;
    private String ciudadOrigen;
    private String ciudadDestino;
    private Double distanciaEstimadaKm;
    private BigDecimal tarifaCalculada;
}

