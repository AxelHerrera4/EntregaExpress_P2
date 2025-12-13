package ec.edu.espe.billing_service.strategy;

import ec.edu.espe.billing_service.model.entity.TarifaBase;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
@Component
public class TarifaNacionalStrategy implements TarifaStrategy{
    @Override
    public BigDecimal calcularTarifa(TarifaBase tarifaBase, Double distanciaKm) {

        BigDecimal recargoPorKm = BigDecimal.valueOf(1.5).multiply(BigDecimal.valueOf(distanciaKm));
        return tarifaBase.getTarifaBase().add(recargoPorKm);
    }
}
