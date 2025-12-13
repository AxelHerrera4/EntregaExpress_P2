package ec.edu.espe.billing_service.model.dto.request;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacturaRequestDTO {

    @NotNull(message = "El id del pedido es obligatorio")
    @Positive(message = "El id del pedido debe ser un número positivo")
    private Long pedidoId;

    @NotNull(message = "El tipo de entrega es obligatorio")
    private String tipoEntrega;

    @NotNull(message = "La distancia en km es obligatoria")
    @Positive(message = "La distancia debe ser un número positivo")
    private Double distanciaKm;

}
