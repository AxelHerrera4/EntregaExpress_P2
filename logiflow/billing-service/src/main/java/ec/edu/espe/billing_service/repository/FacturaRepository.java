package ec.edu.espe.billing_service.repository;

import ec.edu.espe.billing_service.model.entity.Factura;
import ec.edu.espe.billing_service.model.enums.EstadoFactura;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, UUID> {
    Optional<Factura> findByPedidoId(String pedidoId); // UUID como String
    boolean existsByPedidoId(String pedidoId); // UUID como String
    
    // Búsqueda con paginación y filtros
    Page<Factura> findAllByEstado(EstadoFactura estado, Pageable pageable);
    
    Page<Factura> findAllByFechaCreacionBetween(
        LocalDateTime fechaDesde, 
        LocalDateTime fechaHasta, 
        Pageable pageable
    );
    
    Page<Factura> findAllByEstadoAndFechaCreacionBetween(
        EstadoFactura estado,
        LocalDateTime fechaDesde,
        LocalDateTime fechaHasta,
        Pageable pageable
    );
    
    // Estadísticas
    @Query("SELECT COUNT(f) FROM Factura f WHERE f.estado = :estado")
    Long countByEstado(@Param("estado") EstadoFactura estado);
    
    @Query("SELECT COALESCE(SUM(f.montoTotal), 0) FROM Factura f WHERE f.estado = :estado")
    BigDecimal sumMontoByEstado(@Param("estado") EstadoFactura estado);
    
    @Query("SELECT COALESCE(SUM(f.montoTotal), 0) FROM Factura f")
    BigDecimal sumMontoTotal();
    
    @Query("SELECT COALESCE(AVG(f.montoTotal), 0) FROM Factura f")
    Double avgMontoTotal();
}
