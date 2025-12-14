package ec.edu.espe.billing_service.repository;

import ec.edu.espe.billing_service.model.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, UUID> {
    Optional<Factura> findByPedidoId(String pedidoId); // UUID como String
    boolean existsByPedidoId(String pedidoId); // UUID como String
}
