package ec.edu.espe.billing_service.model.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacturaRequestDTO {

    @NotBlank(message = "El id del pedido es obligatorio")
    private String pedidoId; // UUID del pedido como String

    @NotNull(message = "El tipo de entrega es obligatorio")
    private String tipoEntrega;

    @NotNull(message = "La distancia en km es obligatoria")
    @Positive(message = "La distancia debe ser un número positivo")
    private Double distanciaKm;

}
