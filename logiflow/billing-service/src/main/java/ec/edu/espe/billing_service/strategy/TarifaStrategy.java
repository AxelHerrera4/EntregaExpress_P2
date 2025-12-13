package ec.edu.espe.billing_service.strategy;

import ec.edu.espe.billing_service.model.entity.TarifaBase;

import java.math.BigDecimal;

public interface TarifaStrategy {

    BigDecimal calcularTarifa(TarifaBase tarifaBase, Double distanciaKm);

}