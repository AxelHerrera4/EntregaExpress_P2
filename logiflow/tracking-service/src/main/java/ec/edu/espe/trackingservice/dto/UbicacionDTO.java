package ec.edu.espe.trackingservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UbicacionDTO {

    private Long repartidorId;
    private Double latitud;
    private Double longitud;
    private String timestamp;
}

