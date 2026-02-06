package ec.edu.espe.billing_service.event;


import lombok.*;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PedidoEstadoEvent {

    private String pedidoId;
    private String estadoAnterior;
    private String estadoNuevo;
}