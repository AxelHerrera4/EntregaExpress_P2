package ec.edu.espe.billing_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoCreadoEvent implements Serializable {
    private static final long serialVersionUID = 1L;

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