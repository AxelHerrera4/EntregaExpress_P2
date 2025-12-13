package ec.edu.espe.billing_service.strategy;

import ec.edu.espe.billing_service.model.entity.TarifaBase;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DefaultTarifaStrategy implements TarifaStrategy{
    @Override
    public BigDecimal calcularTarifa(TarifaBase tarifaBase, Double distanciaKm) {
        return tarifaBase.getTarifaBase().multiply(BigDecimal.valueOf(distanciaKm));
    }
}
