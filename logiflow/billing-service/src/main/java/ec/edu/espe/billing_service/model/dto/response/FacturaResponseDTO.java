package ec.edu.espe.billing_service.model.dto.response;

import ec.edu.espe.billing_service.model.enums.EstadoFactura;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacturaResponseDTO {

    private UUID id;
    private String pedidoId; // UUID del pedido como String
    private String tipoEntrega;
    private BigDecimal montoTotal;
    private EstadoFactura estado;;
    private LocalDateTime fechaCreacion;
    private Double distanciaKm;

}
