package ec.edu.espe.billing_service.strategy;

import ec.edu.espe.billing_service.model.entity.TarifaBase;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DefaultTarifaStrategyTest {

    private final DefaultTarifaStrategy strategy = new DefaultTarifaStrategy();

    @Test
    void calcularTarifa_multiplicaTarifaPorDistancia() {

        TarifaBase tarifaBase = TarifaBase.builder()
                .tarifaBase(BigDecimal.valueOf(2.50))
                .build();

        BigDecimal resultado = strategy.calcularTarifa(tarifaBase, 10.0);

        // ✔ Comparación correcta de BigDecimal
        assertEquals(0, resultado.compareTo(BigDecimal.valueOf(25)));
    }

    @Test
    void calcularTarifa_distanciaCero_retornaCero() {

        TarifaBase tarifaBase = TarifaBase.builder()
                .tarifaBase(BigDecimal.valueOf(3.00))
                .build();

        BigDecimal resultado = strategy.calcularTarifa(tarifaBase, 0.0);

        assertEquals(0, resultado.compareTo(BigDecimal.ZERO));
    }

    @Test
    void calcularTarifa_tarifaCero_retornaCero() {

        TarifaBase tarifaBase = TarifaBase.builder()
                .tarifaBase(BigDecimal.ZERO)
                .build();

        BigDecimal resultado = strategy.calcularTarifa(tarifaBase, 15.0);

        assertEquals(0, resultado.compareTo(BigDecimal.ZERO));
    }
}
