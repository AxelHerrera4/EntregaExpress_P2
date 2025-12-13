package ec.edu.espe.billing_service.service;

import ec.edu.espe.billing_service.model.dto.request.TarifaBaseRequestDTO;
import ec.edu.espe.billing_service.model.dto.response.TarifaBaseResponseDTO;
import ec.edu.espe.billing_service.model.entity.TarifaBase;

import java.util.List;

public interface TarifaBaseService {
    TarifaBaseResponseDTO crearTarifa(TarifaBaseRequestDTO request);

    TarifaBaseResponseDTO obtenerPorTipoEntrega(String tipoEntrega);

    TarifaBaseResponseDTO actualizarTarifa(String tipoEntrega, TarifaBaseRequestDTO request);

    List<TarifaBaseResponseDTO> obtenerTodasLasTarifas();

    TarifaBase obtenerEntidadPorTipoEntrega(String tipoEntrega);
}
