package ec.edu.espe.billing_service.service.impl;

import ec.edu.espe.billing_service.event.PedidoCreadoEvent;
import ec.edu.espe.billing_service.event.PedidoEstadoEvent;
import ec.edu.espe.billing_service.model.dto.request.FacturaRequestDTO;
import ec.edu.espe.billing_service.model.dto.response.FacturaResponseDTO;
import ec.edu.espe.billing_service.model.enums.EstadoFactura;
import ec.edu.espe.billing_service.model.enums.TipoEntrega;
import ec.edu.espe.billing_service.service.BillingService;
import ec.edu.espe.billing_service.service.FacturaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final FacturaService facturaService;

    @Override
    @Transactional
    public void procesarPedidoCreado(PedidoCreadoEvent event) {
        log.info("💳 [BILLING-SERVICE] Procesando pedido creado | PedidoID: {} | Usuario: {} | MessageID: {}", 
            event.getPedidoId(), event.getUsuarioCreador(), event.getMessageId());
        
        try {
            // Verificar si ya existe una factura para este pedido (idempotencia)
            try {
                FacturaResponseDTO facturaExistente = facturaService.obtenerFacturaPorPedidoId(event.getPedidoId());
                log.warn("⚠️ [IDEMPOTENCIA-BILLING] Ya existe factura para pedido | PedidoID: {} | FacturaID: {} | Usuario: {} | MessageID: {}", 
                    event.getPedidoId(), facturaExistente.getId(), event.getUsuarioCreador(), event.getMessageId());
                return;
            } catch (Exception e) {
                // No existe factura, proceder a crear
                log.info("🛫 [BILLING-SERVICE] No existe factura previa, procediendo a crear | PedidoID: {} | MessageID: {}", 
                    event.getPedidoId(), event.getMessageId());
            }

            // Crear la factura basada en el evento recibido
            log.info("🏦 [BILLING-CREATE] Preparando factura | PedidoID: {} | Cliente: {} | Tipo: {} | Distancia: {} km | MessageID: {}", 
                event.getPedidoId(), event.getClienteId(), event.getTipoEntrega(), event.getDistanciaEstimadaKm(), event.getMessageId());
                
            FacturaRequestDTO facturaRequest = FacturaRequestDTO.builder()
                    .pedidoId(event.getPedidoId())
                    .tipoEntrega(event.getTipoEntrega())
                    .distanciaKm(event.getDistanciaEstimadaKm())
                    .build();

            FacturaResponseDTO facturaResponse = facturaService.crearFactura(facturaRequest);
            
            log.info("✅ [BILLING-SUCCESS] Factura creada exitosamente | FacturaID: {} | PedidoID: {} | Usuario: {} | MessageID: {}", 
                facturaResponse.getId(), event.getPedidoId(), event.getUsuarioCreador(), event.getMessageId());
            log.info("🏁 [CORRELACION-BILLING] FacturaID={} | PedidoID={} | Usuario={} | MessageID={} | Monto={}", 
                facturaResponse.getId(), event.getPedidoId(), event.getUsuarioCreador(), event.getMessageId(), facturaResponse.getMontoTotal());
                
        } catch (Exception e) {
            log.error("❌ [BILLING-ERROR] Error procesando pedido creado | PedidoID={} | Usuario={} | MessageID={} | Error={}", 
                event.getPedidoId(), event.getUsuarioCreador(), event.getMessageId(), e.getMessage(), e);
            throw new RuntimeException("Error al procesar pedido creado", e);
        }
    }

    @Override
    @Transactional
    public void procesarEstadoActualizado(PedidoEstadoEvent event) {
        log.info("💳 [BILLING-SERVICE] Procesando estado actualizado | PedidoID: {} | {}\u2192{} | Usuario: {} | MessageID: {}", 
            event.getPedidoId(), event.getEstadoAnterior(), event.getEstadoNuevo(), event.getUsuarioModificador(), event.getMessageId());
        
        try {
            // Obtener la factura asociada al pedido
            log.info("🔍 [BILLING-SEARCH] Buscando factura existente | PedidoID: {} | MessageID: {}", 
                event.getPedidoId(), event.getMessageId());
                
            FacturaResponseDTO factura = facturaService.obtenerFacturaPorPedidoId(event.getPedidoId());
            
            if (factura == null) {
                log.warn("⚠️ [BILLING-WARNING] No se encontró factura para pedido | PedidoID: {} | MessageID: {}", 
                    event.getPedidoId(), event.getMessageId());
                return;
            }

            log.info("✅ [BILLING-FOUND] Factura encontrada | FacturaID: {} | EstadoActual: {} | PedidoID: {} | MessageID: {}", 
                factura.getId(), factura.getEstado(), event.getPedidoId(), event.getMessageId());

            // Actualizar estado de la factura basado en el estado del pedido
            EstadoFactura nuevoEstadoFactura = mapearEstadoPedidoAFactura(event.getEstadoNuevo());
            
            if (nuevoEstadoFactura != null && !nuevoEstadoFactura.equals(factura.getEstado())) {
                log.info("🔄 [BILLING-UPDATE] Actualizando estado de factura | FacturaID: {} | {}\u2192{} | PedidoID: {} | MessageID: {}", 
                    factura.getId(), factura.getEstado(), nuevoEstadoFactura, event.getPedidoId(), event.getMessageId());
                    
                FacturaResponseDTO facturaActualizada = facturaService.actualizarEstado(
                    factura.getId(), nuevoEstadoFactura);
                    
                log.info("✅ [BILLING-SUCCESS] Estado de factura actualizado exitosamente | FacturaID: {} | {}\u2192{} | PedidoID: {} | Usuario: {} | MessageID: {}", 
                    facturaActualizada.getId(), factura.getEstado(), nuevoEstadoFactura, event.getPedidoId(), event.getUsuarioModificador(), event.getMessageId());
                log.info("🏁 [CORRELACION-BILLING] FacturaID={} | PedidoID={} | Usuario={} | MessageID={} | CambioFactura={}\u2192{}", 
                    facturaActualizada.getId(), event.getPedidoId(), event.getUsuarioModificador(), event.getMessageId(), factura.getEstado(), nuevoEstadoFactura);
            } else {
                log.info("🟡 [BILLING-NOCHANGE] Sin cambios en estado de factura | FacturaID: {} | EstadoActual: {} | PedidoID: {} | MessageID: {}", 
                    factura.getId(), factura.getEstado(), event.getPedidoId(), event.getMessageId());
            }
            
        } catch (Exception e) {
            log.error("❌ [BILLING-ERROR] Error procesando estado actualizado | PedidoID={} | {}\u2192{} | Usuario={} | MessageID={} | Error={}", 
                event.getPedidoId(), event.getEstadoAnterior(), event.getEstadoNuevo(), event.getUsuarioModificador(), event.getMessageId(), e.getMessage(), e);
            throw new RuntimeException("Error al procesar estado actualizado", e);
        }
    }

    /**
     * Mapea el estado del pedido al estado correspondiente de la factura
     */
    private EstadoFactura mapearEstadoPedidoAFactura(String estadoPedido) {
        log.info("🗺️ [MAPPING] Mapeando estado de pedido a factura: {}", estadoPedido);
        
        EstadoFactura estadoFactura = switch (estadoPedido.toUpperCase()) {
            case "CREADO", "ASIGNADO" -> {
                log.info("📊 [MAPPING] {} -> PENDIENTE", estadoPedido);
                yield EstadoFactura.BORRADOR;
            }
            case "EN_TRANSITO", "EN_RUTA" -> {
                log.info("📊 [MAPPING] {} -> PENDIENTE", estadoPedido);
                yield EstadoFactura.BORRADOR;
            }
            case "ENTREGADO" -> {
                log.info("📊 [MAPPING] {} -> PAGADA", estadoPedido);
                yield EstadoFactura.PAGADA;
            }
            case "CANCELADO" -> {
                log.info("📊 [MAPPING] {} -> CANCELADA", estadoPedido);
                yield EstadoFactura.ANULADA;
            }
            default -> {
                log.warn("⚠️ [MAPPING] Estado de pedido no reconocido: {}", estadoPedido);
                yield null;
            }
        };
        
        log.info("🎯 [MAPPING-RESULT] {} -> {}", estadoPedido, estadoFactura);
        return estadoFactura;
    }
}