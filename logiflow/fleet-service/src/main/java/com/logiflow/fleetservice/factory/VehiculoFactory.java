package com.logiflow.fleetservice.factory;


import com.logiflow.fleetservice.model.entity.enums.TipoVehiculo;
import com.logiflow.fleetservice.model.entity.vehiculo.Camion;
import com.logiflow.fleetservice.model.entity.vehiculo.Motorizado;
import com.logiflow.fleetservice.model.entity.vehiculo.VehiculoEntrega;
import com.logiflow.fleetservice.model.entity.vehiculo.VehiculoLiviano;
import org.springframework.stereotype.Component;

/**
 * Factory Pattern para creación de vehículos
 * Cumple con el requisito de "Factory para creación de tipos de entrega"
 */
@Component
public class VehiculoFactory {

  /**
   * Crea un vehículo según el tipo especificado
   * @param tipo Tipo de vehículo a crear
   * @param placa Placa del vehículo
   * @param marca Marca del vehículo
   * @param modelo Modelo del vehículo
   * @param anio Año de fabricación
   * @return Instancia del vehículo creado
   */
  public VehiculoEntrega crearVehiculo(
          TipoVehiculo tipo,
          String placa,
          String marca,
          String modelo,
          Integer anio
  ) {
    VehiculoEntrega vehiculo = crearVehiculoBase(tipo);

    // Configurar propiedades comunes
    vehiculo.setPlaca(placa);
    vehiculo.setMarca(marca);
    vehiculo.setModelo(modelo);
    vehiculo.setAnio(anio);
    vehiculo.setActivo(true);
    vehiculo.setKilometraje(0);

    // Configurar propiedades específicas por tipo
    configurarPropiedadesEspecificas(vehiculo, tipo);

    return vehiculo;
  }

  /**
   * Crea la instancia base del vehículo según su tipo
   */
  private VehiculoEntrega crearVehiculoBase(TipoVehiculo tipo) {
    return switch (tipo) {
      case MOTORIZADO -> new Motorizado();
      case VEHICULO_LIVIANO -> new VehiculoLiviano();
      case CAMION -> new Camion();
    };
  }

  /**
   * Configura propiedades específicas según el tipo de vehículo
   */
  private void configurarPropiedadesEspecificas(VehiculoEntrega vehiculo, TipoVehiculo tipo) {
    switch (tipo) {
      case MOTORIZADO -> {
        vehiculo.setCapacidadCargaKg(50.0);
        vehiculo.setConsumoCombustibleKmPorLitro(35.0);
      }
      case VEHICULO_LIVIANO -> {
        vehiculo.setCapacidadCargaKg(500.0);
        vehiculo.setConsumoCombustibleKmPorLitro(12.0);
      }
      case CAMION -> {
        vehiculo.setCapacidadCargaKg(5000.0);
        vehiculo.setConsumoCombustibleKmPorLitro(6.0);
        if (vehiculo instanceof Camion camion) {
          camion.setNumeroEjes(2);
          camion.setRequiereRampa(false);
        }
      }
    }
  }

  /**
   * Crea un vehículo con configuración personalizada
   */
  public VehiculoEntrega crearVehiculoPersonalizado(
          TipoVehiculo tipo,
          String placa,
          String marca,
          String modelo,
          Integer anio,
          Double capacidadCarga,
          Double consumoCombustible
  ) {
    VehiculoEntrega vehiculo = crearVehiculo(tipo, placa, marca, modelo, anio);

    if (capacidadCarga != null) {
      vehiculo.setCapacidadCargaKg(capacidadCarga);
    }

    if (consumoCombustible != null) {
      vehiculo.setConsumoCombustibleKmPorLitro(consumoCombustible);
    }

    return vehiculo;
  }

  /**
   * Valida si un tipo de vehículo es válido para creación
   */
  public boolean esTipoVehiculoValido(String tipoStr) {
    try {
      TipoVehiculo.valueOf(tipoStr);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}