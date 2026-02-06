package ec.edu.espe.billing_service.service;

import ec.edu.espe.billing_service.model.dto.request.FacturaRequestDTO;
import ec.edu.espe.billing_service.model.dto.response.EstadisticasFacturasDTO;
import ec.edu.espe.billing_service.model.dto.response.FacturaResponseDTO;
import ec.edu.espe.billing_service.model.enums.EstadoFactura;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public interface FacturaService {

    FacturaResponseDTO crearFactura(FacturaRequestDTO request);

    FacturaResponseDTO obtenerFacturaPorId(UUID facturaId);

    FacturaResponseDTO obtenerFacturaPorPedidoId(String pedidoId); // UUID como String
    
    FacturaResponseDTO actualizarEstado(UUID facturaId, EstadoFactura estado);

    Page<FacturaResponseDTO> obtenerTodasLasFacturas(Pageable pageable);
    
    Page<FacturaResponseDTO> obtenerFacturasPorEstado(EstadoFactura estado, Pageable pageable);
    
    Page<FacturaResponseDTO> obtenerFacturasPorFechas(
        LocalDateTime fechaDesde, 
        LocalDateTime fechaHasta, 
        Pageable pageable
    );
    
    Page<FacturaResponseDTO> obtenerFacturasPorEstadoYFechas(
        EstadoFactura estado,
        LocalDateTime fechaDesde,
        LocalDateTime fechaHasta,
        Pageable pageable
    );
    
    EstadisticasFacturasDTO obtenerEstadisticas();
}
