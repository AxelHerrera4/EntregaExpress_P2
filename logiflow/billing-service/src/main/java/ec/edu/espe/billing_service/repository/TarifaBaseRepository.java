package ec.edu.espe.billing_service.repository;

import ec.edu.espe.billing_service.model.entity.TarifaBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TarifaBaseRepository extends JpaRepository<TarifaBase, UUID> {
    Optional<TarifaBase> findByTipoEntrega(String tipoEntrega);

    boolean existsByTipoEntrega(String tipoEntrega);
}
