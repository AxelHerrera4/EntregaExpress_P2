package com.logiflow.fleetservice.repository;


import com.logiflow.fleetservice.model.entity.enums.EstadoRepartidor;
import com.logiflow.fleetservice.model.entity.repartidor.Repartidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepartidorRepository extends JpaRepository<Repartidor, Long> {

  Optional<Repartidor> findByCedula(String cedula);

  Optional<Repartidor> findByEmail(String email);

  List<Repartidor> findByEstado(EstadoRepartidor estado);

  List<Repartidor> findByZonaAsignada(String zona);

  List<Repartidor> findByActivoTrue();

  @Query("SELECT r FROM Repartidor r WHERE r.estado = :estado AND r.activo = true")
  List<Repartidor> findByEstadoAndActivoTrue(@Param("estado") EstadoRepartidor estado);

  @Query("SELECT r FROM Repartidor r WHERE r.estado = 'DISPONIBLE' " +
          "AND r.activo = true " +
          "AND r.vehiculoAsignado IS NOT NULL " +
          "AND r.vehiculoAsignado.activo = true")
  List<Repartidor> findRepartidoresDisponiblesConVehiculo();

  @Query("SELECT r FROM Repartidor r WHERE r.zonaAsignada = :zona " +
          "AND r.estado = 'DISPONIBLE' AND r.activo = true")
  List<Repartidor> findRepartidoresDisponiblesPorZona(@Param("zona") String zona);

  @Query("SELECT r FROM Repartidor r " +
          "WHERE r.estado = 'DISPONIBLE' " +
          "AND r.activo = true " +
          "AND TYPE(r.vehiculoAsignado) = :tipoVehiculo")
  List<Repartidor> findRepartidoresDisponiblesPorTipoVehiculo(
          @Param("tipoVehiculo") Class<?> tipoVehiculo
  );

  @Query("SELECT COUNT(r) FROM Repartidor r WHERE r.estado = :estado")
  long countByEstado(@Param("estado") EstadoRepartidor estado);

  @Query("SELECT r FROM Repartidor r WHERE r.calificacionPromedio >= :minCalificacion " +
          "AND r.activo = true ORDER BY r.calificacionPromedio DESC")
  List<Repartidor> findTopRepartidores(@Param("minCalificacion") Double minCalificacion);

  boolean existsByCedula(String cedula);

  boolean existsByEmail(String email);

  // Métodos adicionales para estadísticas
  @Query("SELECT COALESCE(SUM(r.entregasCompletadas), 0) FROM Repartidor r WHERE r.activo = true")
  Long sumEntregasCompletadas();

  @Query("SELECT COALESCE(SUM(r.entregasFallidas), 0) FROM Repartidor r WHERE r.activo = true")
  Long sumEntregasFallidas();

  @Query("SELECT AVG(r.calificacionPromedio) FROM Repartidor r WHERE r.activo = true AND r.totalCalificaciones > 0")
  Double promedioCalificacionGeneral();
}