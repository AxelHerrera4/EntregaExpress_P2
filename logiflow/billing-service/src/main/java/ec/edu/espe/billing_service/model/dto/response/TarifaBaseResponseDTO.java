package ec.edu.espe.billing_service.model.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarifaBaseResponseDTO {


    private UUID id;
    private String tipoEntrega;
    private BigDecimal tarifaBase;

}
