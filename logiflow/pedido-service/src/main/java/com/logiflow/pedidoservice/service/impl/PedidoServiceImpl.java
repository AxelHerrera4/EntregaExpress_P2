package com.logiflow.pedidoservice.service.impl;

import com.logiflow.pedidoservice.dto.PedidoMapper;
import com.logiflow.pedidoservice.dto.PedidoPatchRequest;
import com.logiflow.pedidoservice.dto.PedidoRequest;
import com.logiflow.pedidoservice.dto.PedidoResponse;
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

        // Guardar pedido
        Pedido savedPedido = pedidoRepository.save(pedido);
        
        log.info("Pedido creado exitosamente con ID: {}", savedPedido.getId());
        
        return pedidoMapper.toResponse(savedPedido);
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
