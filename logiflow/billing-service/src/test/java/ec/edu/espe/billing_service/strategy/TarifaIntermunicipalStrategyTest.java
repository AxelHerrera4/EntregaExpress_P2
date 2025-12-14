package ec.edu.espe.billing_service.strategy;

import ec.edu.espe.billing_service.model.entity.TarifaBase;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;


class TarifaIntermunicipalStrategyTest {

    private final TarifaIntermunicipalStrategy strategy =
            new TarifaIntermunicipalStrategy();

    @Test
    void calcularTarifa_agregaRecargoPorKm() {

        // Arrange
        TarifaBase tarifaBase = TarifaBase.builder()
                .tarifaBase(BigDecimal.valueOf(10.00))
                .build();

        Double distanciaKm = 5.0;
        // recargo = 1.0 * 5 = 5 → total esperado = 15

        // Act
        BigDecimal resultado = strategy.calcularTarifa(tarifaBase, distanciaKm);

        // Assert (comparación correcta de BigDecimal)
        assertEquals(0, resultado.compareTo(BigDecimal.valueOf(15)));
    }

    @Test
    void calcularTarifa_distanciaCero_noAgregaRecargo() {

        TarifaBase tarifaBase = TarifaBase.builder()
                .tarifaBase(BigDecimal.valueOf(8.00))
                .build();

        BigDecimal resultado = strategy.calcularTarifa(tarifaBase, 0.0);

        assertEquals(0, resultado.compareTo(BigDecimal.valueOf(8)));
    }

    @Test
    void calcularTarifa_tarifaBaseCero_retornaSoloRecargo() {

        TarifaBase tarifaBase = TarifaBase.builder()
                .tarifaBase(BigDecimal.ZERO)
                .build();

        BigDecimal resultado = strategy.calcularTarifa(tarifaBase, 10.0);

        // recargo = 1.0 * 10 = 10
        assertEquals(0, resultado.compareTo(BigDecimal.valueOf(10)));
    }
}
