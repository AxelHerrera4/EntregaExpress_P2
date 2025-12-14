package com.logiflow.pedidoservice.service.impl;

import com.logiflow.pedidoservice.model.TipoEntrega;
import com.logiflow.pedidoservice.service.CoberturaValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación simple de validación de cobertura
 * Aplicando principio de Responsabilidad Única (SRP)
 *
 * Soporta coberturas dinámicas basadas en ciudades/provincias:
 * - URBANA-{CIUDAD}: Entregas dentro de la misma ciudad (acepta cualquier ciudad)
 * - INTERMUNICIPAL-{PROVINCIA}: Entregas dentro de la misma provincia (acepta cualquier provincia)
 * - NACIONAL: Entregas entre provincias diferentes
 */
@Slf4j
@Service
public class CoberturaValidationServiceImpl implements CoberturaValidationService {

    // Tipos de entrega disponibles por patrón de cobertura
    private static final Map<String, List<TipoEntrega>> TIPOS_POR_PATRON = new HashMap<>();

    static {
        // Coberturas urbanas: todos los tipos de entrega disponibles
        TIPOS_POR_PATRON.put("URBANA", Arrays.asList(
                TipoEntrega.EXPRESS,
                TipoEntrega.NORMAL,
                TipoEntrega.PROGRAMADA
        ));

        // Coberturas intermunicipales: todos los tipos
        TIPOS_POR_PATRON.put("INTERMUNICIPAL", Arrays.asList(
                TipoEntrega.EXPRESS,
                TipoEntrega.NORMAL,
                TipoEntrega.PROGRAMADA
        ));

        // Cobertura nacional: solo normal y programada
        TIPOS_POR_PATRON.put("NACIONAL", Arrays.asList(
                TipoEntrega.NORMAL,
                TipoEntrega.PROGRAMADA
        ));
    }

    @Override
    public boolean isValidCobertura(String cobertura) {
        if (cobertura == null || cobertura.isEmpty()) {
            log.warn("Cobertura nula o vacía");
            return false;
        }

        String coberturaUpper = cobertura.toUpperCase().trim();
        log.debug("Validando cobertura: {}", coberturaUpper);

        // Validar formato URBANA-{CIUDAD} (acepta cualquier texto después de URBANA-)
        if (coberturaUpper.startsWith("URBANA-")) {
            String ciudad = coberturaUpper.substring(7).trim();
            boolean valida = !ciudad.isEmpty(); // Solo valida que no esté vacío
            log.debug("Cobertura urbana '{}' - Ciudad '{}' - Válida: {}", coberturaUpper, ciudad, valida);
            return valida;
        }

        // Validar formato INTERMUNICIPAL-{PROVINCIA} (acepta cualquier texto después de INTERMUNICIPAL-)
        if (coberturaUpper.startsWith("INTERMUNICIPAL-")) {
            String provincia = coberturaUpper.substring(15).trim();
            boolean valida = !provincia.isEmpty(); // Solo valida que no esté vacío
            log.debug("Cobertura intermunicipal '{}' - Provincia '{}' - Válida: {}", coberturaUpper, provincia, valida);
            return valida;
        }

        // Validar cobertura NACIONAL
        if (coberturaUpper.equals("NACIONAL")) {
            log.debug("Cobertura nacional - Válida: true");
            return true;
        }

        log.warn("Formato de cobertura no reconocido: {}", cobertura);
        return false;
    }

    @Override
    public boolean isTipoEntregaDisponible(TipoEntrega tipoEntrega, String cobertura) {
        if (cobertura == null || tipoEntrega == null) {
            log.warn("Cobertura o tipo de entrega nulo");
            return false;
        }

        String coberturaUpper = cobertura.toUpperCase().trim();
        log.debug("Validando tipo de entrega '{}' para cobertura '{}'", tipoEntrega, coberturaUpper);

        // Determinar el patrón de cobertura
        String patron;
        if (coberturaUpper.startsWith("URBANA-")) {
            patron = "URBANA";
        } else if (coberturaUpper.startsWith("INTERMUNICIPAL-")) {
            patron = "INTERMUNICIPAL";
        } else if (coberturaUpper.equals("NACIONAL")) {
            patron = "NACIONAL";
        } else {
            log.warn("Patrón de cobertura no reconocido: {}", cobertura);
            return false;
        }

        List<TipoEntrega> tiposDisponibles = TIPOS_POR_PATRON.get(patron);
        boolean disponible = tiposDisponibles != null && tiposDisponibles.contains(tipoEntrega);

        log.debug("Patrón: {} - Tipos disponibles: {} - Tipo solicitado: {} - Disponible: {}",
                patron, tiposDisponibles, tipoEntrega, disponible);

        return disponible;
    }
}

