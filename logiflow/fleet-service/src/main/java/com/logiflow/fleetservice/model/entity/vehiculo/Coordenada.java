package com.logiflow.fleetservice.model.entity.vehiculo;


import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Coordenada {

  private Double latitud;
  private Double longitud;


  public double distanciaHasta(Coordenada otra) {
    if (otra == null) {
      return 0.0;
    }

    final int RADIO_TIERRA_KM = 6371;

    double latDistancia = Math.toRadians(otra.latitud - this.latitud);
    double lonDistancia = Math.toRadians(otra.longitud - this.longitud);

    double a = Math.sin(latDistancia / 2) * Math.sin(latDistancia / 2)
            + Math.cos(Math.toRadians(this.latitud)) * Math.cos(Math.toRadians(otra.latitud))
            * Math.sin(lonDistancia / 2) * Math.sin(lonDistancia / 2);

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return RADIO_TIERRA_KM * c;
  }

  public boolean esValida() {
    return latitud != null && longitud != null
            && latitud >= -90 && latitud <= 90
            && longitud >= -180 && longitud <= 180;
  }

  @Override
  public String toString() {
    return String.format("(%.6f, %.6f)", latitud, longitud);
  }
}