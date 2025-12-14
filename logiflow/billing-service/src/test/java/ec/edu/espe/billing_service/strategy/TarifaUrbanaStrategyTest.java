package ec.edu.espe.billing_service.strategy;

import ec.edu.espe.billing_service.model.entity.TarifaBase;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;


class TarifaUrbanaStrategyTest {

    private final TarifaUrbanaStrategy strategy =
            new TarifaUrbanaStrategy();

    @Test
    void calcularTarifa_agregaRecargoUrbanoPorKm() {

        // Arrange
        TarifaBase tarifaBase = TarifaBase.builder()
                .tarifaBase(BigDecimal.valueOf(5.00))
                .build();

        Double distanciaKm = 10.0;
        // recargo = 0.5 * 10 = 5 → total esperado = 10

        // Act
        BigDecimal resultado = strategy.calcularTarifa(tarifaBase, distanciaKm);

        // Assert (comparación correcta de BigDecimal)
        assertEquals(0, resultado.compareTo(BigDecimal.valueOf(10)));
    }

    @Test
    void calcularTarifa_distanciaCero_noAgregaRecargo() {

        TarifaBase tarifaBase = TarifaBase.builder()
                .tarifaBase(BigDecimal.valueOf(7.00))
                .build();

        BigDecimal resultado = strategy.calcularTarifa(tarifaBase, 0.0);

        assertEquals(0, resultado.compareTo(BigDecimal.valueOf(7)));
    }

    @Test
    void calcularTarifa_tarifaBaseCero_retornaSoloRecargo() {

        TarifaBase tarifaBase = TarifaBase.builder()
                .tarifaBase(BigDecimal.ZERO)
                .build();

        BigDecimal resultado = strategy.calcularTarifa(tarifaBase, 6.0);

        // recargo = 0.5 * 6 = 3
        assertEquals(0, resultado.compareTo(BigDecimal.valueOf(3)));
    }
}
