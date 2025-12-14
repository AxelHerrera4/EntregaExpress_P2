package ec.edu.espe.billing_service.strategy;

import ec.edu.espe.billing_service.model.entity.TarifaBase;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;


class TarifaNacionalStrategyTest {

    private final TarifaNacionalStrategy strategy =
            new TarifaNacionalStrategy();

    @Test
    void calcularTarifa_agregaRecargoNacionalPorKm() {

        // Arrange
        TarifaBase tarifaBase = TarifaBase.builder()
                .tarifaBase(BigDecimal.valueOf(20.00))
                .build();

        Double distanciaKm = 10.0;
        // recargo = 1.5 * 10 = 15 → total esperado = 35

        // Act
        BigDecimal resultado = strategy.calcularTarifa(tarifaBase, distanciaKm);

        // Assert (comparación correcta de BigDecimal)
        assertEquals(0, resultado.compareTo(BigDecimal.valueOf(35)));
    }

    @Test
    void calcularTarifa_distanciaCero_noAgregaRecargo() {

        TarifaBase tarifaBase = TarifaBase.builder()
                .tarifaBase(BigDecimal.valueOf(12.00))
                .build();

        BigDecimal resultado = strategy.calcularTarifa(tarifaBase, 0.0);

        assertEquals(0, resultado.compareTo(BigDecimal.valueOf(12)));
    }

    @Test
    void calcularTarifa_tarifaBaseCero_retornaSoloRecargo() {

        TarifaBase tarifaBase = TarifaBase.builder()
                .tarifaBase(BigDecimal.ZERO)
                .build();

        BigDecimal resultado = strategy.calcularTarifa(tarifaBase, 8.0);

        // recargo = 1.5 * 8 = 12
        assertEquals(0, resultado.compareTo(BigDecimal.valueOf(12)));
    }
}
