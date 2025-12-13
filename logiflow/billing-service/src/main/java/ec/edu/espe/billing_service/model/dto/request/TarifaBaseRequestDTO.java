package ec.edu.espe.billing_service.model.dto.request;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarifaBaseRequestDTO {

    @NotNull(message = "El tipo de entrega es obligatorio")
    private String tipoEntrega;

    @NotNull(message = "La tarifa base es obligatoria")
    @DecimalMin(value = "0.01", inclusive = true, message = "La tarifa base debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "La tarifa base debe tener máximo 2 decimales")
    private BigDecimal tarifaBase;

}
