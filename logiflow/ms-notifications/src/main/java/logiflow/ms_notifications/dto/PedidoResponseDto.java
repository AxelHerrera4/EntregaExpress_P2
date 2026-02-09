package logiflow.ms_notifications.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO para la respuesta del Pedido desde pedido-service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PedidoResponseDto {
    private UUID id;
    private String clienteId;
    private Double peso;
    private String estado;
    private String cobertura;
    private String repartidorId;
    
    // Cliente info
    private ClienteDto cliente;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClienteDto {
        private String id;
        private String nombre;
        private String email;
        private String telefono;
        private String direccion;
    }
}
