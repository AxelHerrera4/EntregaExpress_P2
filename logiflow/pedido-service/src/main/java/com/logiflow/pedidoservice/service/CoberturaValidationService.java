package com.logiflow.pedidoservice.service;

import com.logiflow.pedidoservice.model.TipoEntrega;

/**
 * Servicio para validar la cobertura geográfica
 * Patrón: Strategy - permite diferentes implementaciones de validación
 */
public interface CoberturaValidationService {

    /**
     * Valida si la cobertura geográfica es válida
     * @param cobertura zona geográfica
     * @return true si es válida, false en caso contrario
     */
    boolean isValidCobertura(String cobertura);

    /**
     * Valida si el tipo de entrega está disponible para la cobertura
     * @param tipoEntrega tipo de entrega
     * @param cobertura zona geográfica
     * @return true si está disponible
     */
    boolean isTipoEntregaDisponible(TipoEntrega tipoEntrega, String cobertura);
}

