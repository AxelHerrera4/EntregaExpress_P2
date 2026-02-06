package com.logiflow.fleetservice.model.interfaces;

import com.logiflow.fleetservice.dto.InformacionRuta;

/**
 * Contrato que define las capacidades de un vehículo para ser incluido en rutas.
 * Permite al RoutingService obtener información necesaria sin conocer el tipo específico.
 */
public interface IRuteable {
  
  /**
   * Provee información estructurada del vehículo para cálculo de rutas
   * @return InformacionRuta con los datos necesarios para el Routing Service
   */
  InformacionRuta getInformacionRuta();
}
