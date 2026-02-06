package ec.edu.espe.billing_service.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasFacturasDTO {
    
    private Long totalFacturas;
    private Long totalPagadas;
    private Long totalPendientes;
    private Long totalBorrador;
    private Long totalCanceladas;
    
    private BigDecimal montoTotalFacturado;
    private BigDecimal montoTotalPagado;
    private BigDecimal montoTotalPendiente;
    
    private Double promedioMontoPorFactura;
}
