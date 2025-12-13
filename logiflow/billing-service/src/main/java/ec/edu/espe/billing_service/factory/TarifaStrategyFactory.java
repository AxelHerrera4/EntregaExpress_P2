package ec.edu.espe.billing_service.factory;

import ec.edu.espe.billing_service.strategy.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TarifaStrategyFactory {

    private final TarifaUrbanaStrategy urbanaStrategy;
    private final TarifaIntermunicipalStrategy intermunicipalStrategy;
    private final TarifaNacionalStrategy nacionalStrategy;
    private final DefaultTarifaStrategy defaultTarifaStrategy;

    public TarifaStrategy obtenerStrategy(String tipoEntrega) {
        if (tipoEntrega == null) {
            throw new IllegalArgumentException("Tipo de entrega no puede ser null");
        }

        return switch (tipoEntrega.toUpperCase()) {
            case "URBANA" -> urbanaStrategy;
            case "INTERMUNICIPAL" -> intermunicipalStrategy;
            case "NACIONAL" -> nacionalStrategy;
            default -> defaultTarifaStrategy;


        };
    }
}

