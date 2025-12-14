package ec.edu.espe.billing_service.factory;

import ec.edu.espe.billing_service.strategy.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class TarifaStrategyFactoryTest {

    private TarifaStrategyFactory factory;

    @BeforeEach
    void setup() {
        factory = new TarifaStrategyFactory(
                new TarifaUrbanaStrategy(),
                new TarifaIntermunicipalStrategy(),
                new TarifaNacionalStrategy(),
                new DefaultTarifaStrategy()
        );
    }

    @Test
    void obtenerStrategy_urbana() {
        TarifaStrategy strategy = factory.obtenerStrategy("URBANA");
        assertTrue(strategy instanceof TarifaUrbanaStrategy);
    }

    @Test
    void obtenerStrategy_intermunicipal() {
        TarifaStrategy strategy = factory.obtenerStrategy("INTERMUNICIPAL");
        assertTrue(strategy instanceof TarifaIntermunicipalStrategy);
    }

    @Test
    void obtenerStrategy_nacional() {
        TarifaStrategy strategy = factory.obtenerStrategy("NACIONAL");
        assertTrue(strategy instanceof TarifaNacionalStrategy);
    }

    @Test
    void obtenerStrategy_default() {
        TarifaStrategy strategy = factory.obtenerStrategy("OTRO");
        assertTrue(strategy instanceof DefaultTarifaStrategy);
    }

    @Test
    void obtenerStrategy_minusculas() {
        TarifaStrategy strategy = factory.obtenerStrategy("urbana");
        assertTrue(strategy instanceof TarifaUrbanaStrategy);
    }

    @Test
    void obtenerStrategy_tipoNull_lanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> factory.obtenerStrategy(null)
        );

        assertEquals("Tipo de entrega no puede ser null", ex.getMessage());
    }
}
