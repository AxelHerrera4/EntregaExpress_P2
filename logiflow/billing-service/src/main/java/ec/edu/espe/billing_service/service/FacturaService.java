package ec.edu.espe.billing_service.service;

import ec.edu.espe.billing_service.model.dto.request.FacturaRequestDTO;
import ec.edu.espe.billing_service.model.dto.response.FacturaResponseDTO;
import ec.edu.espe.billing_service.model.enums.EstadoFactura;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface FacturaService {

    FacturaResponseDTO crearFactura(FacturaRequestDTO request);

    FacturaResponseDTO obtenerFacturaPorId(UUID facturaId);

    FacturaResponseDTO obtenerFacturaPorPedidoId(Long pedidoId);
    FacturaResponseDTO actualizarEstado(UUID facturaId, EstadoFactura estado);

}
