package com.logiflow.fleetservice.model.interfaces;

import com.logiflow.fleetservice.model.entity.vehiculo.Coordenada;

import java.util.List;

/**
 * Interfaz que define el contrato para entidades que pueden generar rutas
 * Cumple con el requisito de "interfaces para definir contratos de comportamiento interoperable"
 */
public interface IRuteable {

  /**
   * Genera una ruta óptima entre dos puntos
   * @param origen Coordenada de origen
   * @param destino Coordenada de destino
   * @return Lista de coordenadas que conforman la ruta
   */
  List<Coordenada> generarRuta(Coordenada origen, Coordenada destino);

  /**
   * Calcula la distancia estimada en kilómetros entre dos puntos
   * @param origen Coordenada de origen
   * @param destino Coordenada de destino
   * @return Distancia en kilómetros
   */
  double calcularDistancia(Coordenada origen, Coordenada destino);

  /**
   * Estima el tiempo de viaje en minutos
   * @param origen Coordenada de origen
   * @param destino Coordenada de destino
   * @return Tiempo estimado en minutos
   */
  int estimarTiempoViaje(Coordenada origen, Coordenada destino);

  /**
   * Verifica si el vehículo puede realizar la ruta según sus características
   * @param distanciaKm Distancia de la ruta
   * @return true si puede realizar la ruta
   */
  boolean puedeRealizarRuta(double distanciaKm);
}
