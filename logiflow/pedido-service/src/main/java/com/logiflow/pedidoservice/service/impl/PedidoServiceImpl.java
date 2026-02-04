package com.logiflow.pedidoservice.service.impl;

import com.logiflow.pedidoservice.client.BillingClient;
import com.logiflow.pedidoservice.client.FleetClient;
import com.logiflow.pedidoservice.dto.*;
import com.logiflow.pedidoservice.model.EstadoPedido;
import com.logiflow.pedidoservice.model.ModalidadServicio;
import com.logiflow.pedidoservice.model.Pedido;
import com.logiflow.pedidoservice.model.TipoEntrega;
import com.logiflow.pedidoservice.repository.PedidoRepository;
import com.logiflow.pedidoservice.service.CoberturaValidationService;
import com.logiflow.pedidoservice.service.PedidoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de pedidos
 * Aplicando principios SOLID:
 * - SRP: Responsabilidad única de gestión de pedidos
 * - OCP: Abierto para extensión (interfaces), cerrado para modificación
 * - LSP: Respeta el contrato de la interface PedidoService
 * - ISP: Interface segregada con métodos específicos
 * - DIP: Depende de abstracciones (interfaces) no de implementaciones
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoMapper pedidoMapper;
    private final CoberturaValidationService coberturaValidationService;
    private final BillingClient billingClient;
    private final FleetClient fleetClient;

    @Value("${integration.billing.enabled:true}")
    private boolean billingIntegrationEnabled;

    @Value("${integration.fleet.enabled:true}")
    private boolean fleetIntegrationEnabled;

    @Override
    @Transactional
    public PedidoResponse createPedido(PedidoRequest request) {
        log.info("Creando nuevo pedido para cliente: {}", request.getClienteId());
        
        // Convertir request a entidad (cobertura se calcula en el mapper)
        Pedido pedido = pedidoMapper.toEntity(request);
        
        // Validar cobertura geográfica (usamos la cobertura calculada)
        validateCobertura(pedido.getCobertura());

        // Validar tipo de entrega disponible para la cobertura
        validateTipoEntrega(request.getTipoEntrega(), pedido.getCobertura());

        // Guardar pedido primero para obtener su ID
        Pedido savedPedido = pedidoRepository.save(pedido);
        log.info("Pedido creado con ID: {}", savedPedido.getId());

        // ============= INTEGRACIÓN CON BILLING SERVICE =============
        if (billingIntegrationEnabled) {
            try {
                log.info("Integrando con Billing Service para crear factura...");

                // Calcular distancia estimada (puedes implementar lógica más sofisticada)
                Double distanciaKm = calcularDistanciaEstimada(
                    savedPedido.getDireccionOrigen().getCiudad(),
                    savedPedido.getDireccionDestino().getCiudad(),
                    savedPedido.getModalidadServicio()
                );

                // Crear request para Billing Service
                FacturaRequest facturaRequest = FacturaRequest.builder()
                        .pedidoId(savedPedido.getId())
                        .tipoEntrega(savedPedido.getTipoEntrega().name())
                        .distanciaKm(distanciaKm)
                        .build();

                // Llamar a Billing Service
                FacturaResponse facturaResponse = billingClient.crearFactura(facturaRequest);

                // Asociar factura al pedido
                savedPedido.setFacturaId(facturaResponse.getId());
                savedPedido.setTarifaCalculada(facturaResponse.getMontoTotal().doubleValue());

                // Actualizar pedido con la factura
                savedPedido = pedidoRepository.save(savedPedido);

                log.info("Factura creada y asociada: ID={}, Monto={}",
                        facturaResponse.getId(), facturaResponse.getMontoTotal());

            } catch (Exception e) {
                log.error("Error al integrar con Billing Service: {}", e.getMessage());
                log.warn("El pedido fue creado pero sin factura. Crear factura manualmente más tarde.");
                // No lanzamos la excepción para no bloquear la creación del pedido
            }
        } else {
            log.info("Integración con Billing Service deshabilitada");
        }

        // ============= INTEGRACIÓN CON FLEET SERVICE (OPCIONAL) =============
        if (fleetIntegrationEnabled) {
            try {
                log.info("Integrando con Fleet Service para asignar repartidor...");

                // Crear request para Fleet Service
                AsignacionRequest asignacionRequest = AsignacionRequest.builder()
                        .pedidoId(savedPedido.getId())
                        .modalidadServicio(savedPedido.getModalidadServicio().name())
                        .tipoEntrega(savedPedido.getTipoEntrega().name())
                        .prioridad(savedPedido.getPrioridad().name())
                        .ciudadOrigen(savedPedido.getDireccionOrigen().getCiudad())
                        .ciudadDestino(savedPedido.getDireccionDestino().getCiudad())
                        .peso(savedPedido.getPeso())
                        .build();

                // Llamar a Fleet Service
                AsignacionResponse asignacionResponse = fleetClient.asignarRepartidor(asignacionRequest);

                // Si la asignación fue exitosa, actualizar el pedido
                if ("ASIGNADO".equals(asignacionResponse.getEstado())) {
                    savedPedido.setRepartidorId(asignacionResponse.getRepartidorId());
                    savedPedido.setVehiculoId(asignacionResponse.getVehiculoId());
                    savedPedido.setEstado(EstadoPedido.ASIGNADO);

                    savedPedido = pedidoRepository.save(savedPedido);

                    log.info("Repartidor asignado: ID={}, Vehículo={}",
                            asignacionResponse.getRepartidorId(), asignacionResponse.getVehiculoId());
                } else {
                    log.warn("No se pudo asignar repartidor: {}", asignacionResponse.getMensaje());
                }

            } catch (Exception e) {
                log.error("Error al integrar con Fleet Service: {}", e.getMessage());
                log.warn("El pedido quedará en estado PENDIENTE para asignación manual");
                // No lanzamos la excepción para no bloquear la creación del pedido
            }
        } else {
            log.info("Integración con Fleet Service deshabilitada o no disponible");
        }

        log.info("Pedido creado exitosamente - ID: {}, Estado: {}",
                savedPedido.getId(), savedPedido.getEstado());

        return pedidoMapper.toResponse(savedPedido);
    }

    /**
     * Calcula distancia estimada entre origen y destino
     * TODO: Implementar cálculo real usando API de mapas o base de datos de distancias
     */
    private Double calcularDistanciaEstimada(String ciudadOrigen, String ciudadDestino, ModalidadServicio modalidad) {
        // Por ahora devolvemos estimación basada en modalidad
        return switch (modalidad) {
            case URBANA_RAPIDA -> 10.0; // ~10 km para entregas urbanas
            case INTERMUNICIPAL -> 50.0; // ~50 km para entregas intermunicipales
            case NACIONAL -> 200.0; // ~200 km para entregas nacionales
        };
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponse getPedidoById(String id) {
        log.info("Consultando pedido con ID: {}", id);
        
        Pedido pedido = findPedidoOrThrow(id);
        
        return pedidoMapper.toResponse(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> getAllPedidos() {
        log.info("Consultando todos los pedidos");
        
        return pedidoRepository.findAll()
                .stream()
                .map(pedidoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> getPedidosByCliente(String clienteId) {
        log.info("Consultando pedidos del cliente: {}", clienteId);
        
        return pedidoRepository.findByClienteId(clienteId)
                .stream()
                .map(pedidoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PedidoResponse patchPedido(String id, PedidoPatchRequest patchRequest) {
        log.info("Actualizando parcialmente pedido con ID: {}", id);
        
        Pedido pedido = findPedidoOrThrow(id);
        
        // Validar que el pedido no esté cancelado o entregado
        validatePedidoCanBeModified(pedido);
        
        // Validar cambios de cobertura y tipo de entrega si aplican
        if (patchRequest.getCobertura() != null) {
            validateCobertura(patchRequest.getCobertura());
            
            // Si también cambia el tipo de entrega, validar compatibilidad
            TipoEntrega tipoEntrega = patchRequest.getTipoEntrega() != null
                    ? patchRequest.getTipoEntrega()
                    : pedido.getTipoEntrega();
            validateTipoEntrega(tipoEntrega, patchRequest.getCobertura());
        } else if (patchRequest.getTipoEntrega() != null) {
            // Solo cambió tipo de entrega, validar con cobertura actual
            validateTipoEntrega(patchRequest.getTipoEntrega(), pedido.getCobertura());
        }
        
        // Actualizar campos del pedido
        pedidoMapper.updateEntityFromPatch(pedido, patchRequest);
        
        // Guardar cambios
        Pedido updatedPedido = pedidoRepository.save(pedido);
        
        log.info("Pedido actualizado exitosamente con ID: {}", updatedPedido.getId());
        
        return pedidoMapper.toResponse(updatedPedido);
    }

    @Override
    @Transactional
    public PedidoResponse cancelarPedido(String id) {
        log.info("Cancelando pedido con ID: {}", id);
        
        Pedido pedido = findPedidoOrThrow(id);
        
        // Validar que el pedido pueda ser cancelado
        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new IllegalStateException("El pedido ya está cancelado");
        }
        
        if (pedido.getEstado() == EstadoPedido.ENTREGADO) {
            throw new IllegalStateException("No se puede cancelar un pedido ya entregado");
        }
        
        // Si el pedido tiene repartidor asignado, liberar en Fleet Service
        if (fleetIntegrationEnabled && pedido.getRepartidorId() != null) {
            try {
                log.info("Liberando asignación en Fleet Service...");
                fleetClient.liberarAsignacion(id);
            } catch (Exception e) {
                log.error("Error al liberar asignación en Fleet Service: {}", e.getMessage());
                // Continuamos con la cancelación aunque falle la liberación
            }
        }

        // Cambiar estado a cancelado
        pedido.setEstado(EstadoPedido.CANCELADO);

        Pedido canceledPedido = pedidoRepository.save(pedido);
        
        log.info("Pedido cancelado exitosamente con ID: {}", canceledPedido.getId());
        
        return pedidoMapper.toResponse(canceledPedido);
    }

    @Override
    @Transactional
    public void deletePedido(String id) {
        log.info("Eliminando pedido con ID: {}", id);
        
        Pedido pedido = findPedidoOrThrow(id);
        
        pedidoRepository.delete(pedido);
        
        log.info("Pedido eliminado exitosamente con ID: {}", id);
    }

    // Métodos privados de ayuda
    
    private Pedido findPedidoOrThrow(String id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Pedido no encontrado con ID: " + id));
    }

    private void validateCobertura(String cobertura) {
        if (!coberturaValidationService.isValidCobertura(cobertura)) {
            throw new IllegalArgumentException(
                    "Cobertura geográfica no válida: " + cobertura);
        }
    }

    private void validateTipoEntrega(TipoEntrega tipoEntrega, String cobertura) {
        if (!coberturaValidationService.isTipoEntregaDisponible(tipoEntrega, cobertura)) {
            throw new IllegalArgumentException(
                    "Tipo de entrega " + tipoEntrega + " no disponible para la cobertura: " + cobertura);
        }
    }

    private void validatePedidoCanBeModified(Pedido pedido) {
        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new IllegalStateException("No se puede modificar un pedido cancelado");
        }
        if (pedido.getEstado() == EstadoPedido.ENTREGADO) {
            throw new IllegalStateException("No se puede modificar un pedido ya entregado");
        }
    }

    // ==================== MÉTODOS PARA INTEGRACIÓN CON FLEETSERVICE ====================

    @Override
    @Transactional
    public PedidoResponse asignarRepartidorYVehiculo(String pedidoId, String repartidorId, String vehiculoId) {
        log.info("Asignando repartidor {} y vehículo {} al pedido {}", repartidorId, vehiculoId, pedidoId);

        Pedido pedido = findPedidoOrThrow(pedidoId);

        // Validar que el pedido pueda ser asignado
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden asignar recursos a pedidos en estado PENDIENTE");
        }

        // Asignar recursos
        pedido.setRepartidorId(repartidorId);
        pedido.setVehiculoId(vehiculoId);
        pedido.setEstado(EstadoPedido.ASIGNADO);

        Pedido updatedPedido = pedidoRepository.save(pedido);

        log.info("Pedido {} asignado exitosamente", pedidoId);

        return pedidoMapper.toResponse(updatedPedido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> getPedidosPendientesAsignacion() {
        log.info("Consultando pedidos pendientes de asignación");

        return pedidoRepository.findPedidosPendientesAsignacion()
                .stream()
                .map(pedidoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> getPedidosByRepartidor(String repartidorId) {
        log.info("Consultando pedidos del repartidor: {}", repartidorId);

        return pedidoRepository.findByRepartidorId(repartidorId)
                .stream()
                .map(pedidoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> getPedidosByModalidad(ModalidadServicio modalidad) {
        log.info("Consultando pedidos por modalidad: {}", modalidad);

        return pedidoRepository.findByModalidadServicio(modalidad)
                .stream()
                .map(pedidoMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ==================== MÉTODOS PARA INTEGRACIÓN CON BILLINGSERVICE ====================

    @Override
    @Transactional
    public PedidoResponse asociarFactura(String pedidoId, String facturaId, Double tarifa) {
        log.info("Asociando factura {} con tarifa {} al pedido {}", facturaId, tarifa, pedidoId);

        Pedido pedido = findPedidoOrThrow(pedidoId);

        // Validar que no tenga factura ya asociada
        if (pedido.getFacturaId() != null) {
            log.warn("El pedido {} ya tiene una factura asociada: {}", pedidoId, pedido.getFacturaId());
            throw new IllegalStateException("El pedido ya tiene una factura asociada");
        }

        // Asociar factura
        pedido.setFacturaId(facturaId);
        pedido.setTarifaCalculada(tarifa);

        Pedido updatedPedido = pedidoRepository.save(pedido);

        log.info("Factura asociada exitosamente al pedido {}", pedidoId);

        return pedidoMapper.toResponse(updatedPedido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> getPedidosSinFactura() {
        log.info("Consultando pedidos sin factura");

        return pedidoRepository.findPedidosSinFactura()
                .stream()
                .map(pedidoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> getPedidosAltaPrioridad() {
        log.info("Consultando pedidos de alta prioridad pendientes");

        return pedidoRepository.findPedidosAltaPrioridadPendientes()
                .stream()
                .map(pedidoMapper::toResponse)
                .collect(Collectors.toList());
    }
}
