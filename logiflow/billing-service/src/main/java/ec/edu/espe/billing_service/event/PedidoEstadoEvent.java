package ec.edu.espe.billing_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoEstadoEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    // Para idempotencia
    private String messageId;
    private LocalDateTime timestamp;
    
    // Información del evento
    private String pedidoId;
    private String estadoAnterior;
    private String estadoNuevo;
    private String usuarioModificador; // Usuario que modificó el estado
    private String repartidorId;
    private String vehiculoId;
}