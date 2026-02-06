package ec.edu.espe.billing_service.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class RepartidorUbicacionEvent {
    
    private String messageId;
    private LocalDateTime timestamp;
    private String repartidorId;
    private String pedidoId;
    private String vehiculoId;
    private Double latitud;
    private Double longitud;
    private String direccion;
    private String estadoRepartidor;
    private Double velocidadKmh;
    private Integer bateriaPorcentaje;
    private Boolean enRuta;
    
    public RepartidorUbicacionEvent(String repartidorId, String pedidoId, String vehiculoId, 
                                  Double latitud, Double longitud, String direccion) {
        this.messageId = java.util.UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.repartidorId = repartidorId;
        this.pedidoId = pedidoId;
        this.vehiculoId = vehiculoId;
        this.latitud = latitud;
        this.longitud = longitud;
        this.direccion = direccion;
    }
}