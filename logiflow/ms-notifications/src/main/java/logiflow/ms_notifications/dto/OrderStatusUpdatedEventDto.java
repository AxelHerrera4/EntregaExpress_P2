package logiflow.ms_notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdatedEventDto {
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

