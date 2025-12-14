package com.logiflow.fleetservice.model.interfaces;

import com.logiflow.fleetservice.model.entity.vehiculo.Coordenada;

import java.time.LocalDateTime;

/**
 * Interfaz para entidades que pueden registrar su posición GPS
 */
public interface IRegistrableGPS {

  /**
   * Registra la ubicación actual del vehículo
   * @param coordenada Coordenadas GPS
   * @param timestamp Momento del registro
   */
  void registrarUbicacion(Coordenada coordenada, LocalDateTime timestamp);

  /**
   * Obtiene la última ubicación registrada
   * @return Coordenada de la última posición conocida
   */
  Coordenada obtenerUltimaUbicacion();

  /**
   * Obtiene el timestamp de la última actualización
   * @return Fecha y hora de la última actualización
   */
  LocalDateTime obtenerUltimaActualizacion();

  /**
   * Verifica si la ubicación está actualizada (menos de X minutos)
   * @param minutosMaximos Minutos máximos de antigüedad
   * @return true si está actualizada
   */
  boolean ubicacionActualizada(int minutosMaximos);
}