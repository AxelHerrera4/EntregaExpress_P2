package com.logiflow.fleetservice.repository;


import com.logiflow.fleetservice.model.entity.enums.TipoVehiculo;
import com.logiflow.fleetservice.model.entity.vehiculo.VehiculoEntrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculoRepository extends JpaRepository<VehiculoEntrega, Long> {

  Optional<VehiculoEntrega> findByPlaca(String placa);

  List<VehiculoEntrega> findByActivoTrue();

  @Query("SELECT v FROM VehiculoEntrega v WHERE v.activo = true " +
          "AND TYPE(v) = :tipo")
  List<VehiculoEntrega> findVehiculosActivosPorTipo(@Param("tipo") Class<? extends VehiculoEntrega> tipo);

  @Query("SELECT v FROM VehiculoEntrega v WHERE v.activo = true " +
          "AND v.id NOT IN (SELECT r.vehiculoAsignado.id FROM Repartidor r WHERE r.vehiculoAsignado IS NOT NULL)")
  List<VehiculoEntrega> findVehiculosDisponibles();

  @Query("SELECT COUNT(v) FROM VehiculoEntrega v WHERE v.activo = true")
  long countVehiculosActivos();

  @Query("SELECT v FROM VehiculoEntrega v WHERE v.marca = :marca AND v.activo = true")
  List<VehiculoEntrega> findByMarcaAndActivoTrue(@Param("marca") String marca);

  boolean existsByPlaca(String placa);

  // Métodos adicionales para estadísticas
  long countByActivoTrue();

  @Query("SELECT COUNT(v) FROM VehiculoEntrega v WHERE v.activo = true " +
          "AND v.id NOT IN (SELECT r.vehiculoAsignado.id FROM Repartidor r WHERE r.vehiculoAsignado IS NOT NULL)")
  long countVehiculosDisponibles();

  @Query("SELECT COUNT(v) FROM VehiculoEntrega v WHERE v.activo = true " +
          "AND TYPE(v) = :tipo")
  long countByTipoAndActivoTrue(@Param("tipo") Class<? extends VehiculoEntrega> tipo);
}