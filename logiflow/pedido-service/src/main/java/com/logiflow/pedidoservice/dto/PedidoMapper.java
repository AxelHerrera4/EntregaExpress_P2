package com.logiflow.pedidoservice.dto;

import com.logiflow.pedidoservice.model.Pedido;
import org.springframework.stereotype.Component;

@Component
public class PedidoMapper {

    /**
     * Convierte un PedidoRequest a entidad Pedido
     */
    public Pedido toEntity(PedidoRequest request) {
        return Pedido.builder()
                .clienteId(request.getClienteId())
                .direccionOrigen(request.getDireccionOrigen())
                .direccionDestino(request.getDireccionDestino())
                .modalidadServicio(request.getModalidadServicio())
                .tipoEntrega(request.getTipoEntrega())
                .peso(request.getPeso())
                .volumen(null)  // Campo opcional, se calcula o se asigna después si es necesario
                .cobertura(determinarCobertura(request.getDireccionOrigen(), request.getDireccionDestino()))  // Se calcula automáticamente
                .descripcion(null)  // Campo opcional
                .telefonoContacto(request.getTelefonoContacto())
                .nombreDestinatario(request.getNombreDestinatario())
                .fechaEstimadaEntrega(null)  // Se calcula después según modalidad y tipo
                .prioridad(null)  // Se asigna automáticamente en @PrePersist según tipoEntrega
                .estado(com.logiflow.pedidoservice.model.EstadoPedido.PENDIENTE)
                .build();
    }

    /**
     * Determina la cobertura geográfica basándose en las direcciones
     */
    private String determinarCobertura(com.logiflow.pedidoservice.model.Direccion origen,
                                      com.logiflow.pedidoservice.model.Direccion destino) {
        // Lógica simple: si ciudad coincide = URBANA, si provincia coincide = INTERMUNICIPAL, sino = NACIONAL
        if (origen.getCiudad().equalsIgnoreCase(destino.getCiudad())) {
            return "URBANA-" + origen.getCiudad().toUpperCase();
        } else if (origen.getProvincia().equalsIgnoreCase(destino.getProvincia())) {
            return "INTERMUNICIPAL-" + origen.getProvincia().toUpperCase();
        } else {
            return "NACIONAL";
        }
    }

    /**
     * Convierte una entidad Pedido a PedidoResponse
     */
    public PedidoResponse toResponse(Pedido pedido) {
        return PedidoResponse.builder()
                .id(pedido.getId())
                .clienteId(pedido.getClienteId())
                .direccionOrigen(pedido.getDireccionOrigen())
                .direccionDestino(pedido.getDireccionDestino())
                .modalidadServicio(pedido.getModalidadServicio())
                .tipoEntrega(pedido.getTipoEntrega())
                .estado(pedido.getEstado())
                .peso(pedido.getPeso())
                .telefonoContacto(pedido.getTelefonoContacto())
                .nombreDestinatario(pedido.getNombreDestinatario())
                .fechaCreacion(pedido.getFechaCreacion())
                .build();
    }

    /**
     * Actualiza un Pedido existente con los datos del PedidoPatchRequest
     * Solo actualiza los campos que no son nulos
     */
    public void updateEntityFromPatch(Pedido pedido, PedidoPatchRequest patchRequest) {
        if (patchRequest.getDireccionOrigen() != null) {
            pedido.setDireccionOrigen(patchRequest.getDireccionOrigen());
        }
        if (patchRequest.getDireccionDestino() != null) {
            pedido.setDireccionDestino(patchRequest.getDireccionDestino());
        }
        if (patchRequest.getModalidadServicio() != null) {
            pedido.setModalidadServicio(patchRequest.getModalidadServicio());
        }
        if (patchRequest.getTipoEntrega() != null) {
            pedido.setTipoEntrega(patchRequest.getTipoEntrega());
        }
        if (patchRequest.getEstado() != null) {
            pedido.setEstado(patchRequest.getEstado());
        }
        if (patchRequest.getPrioridad() != null) {
            pedido.setPrioridad(patchRequest.getPrioridad());
        }
        if (patchRequest.getPeso() != null) {
            pedido.setPeso(patchRequest.getPeso());
        }
        if (patchRequest.getVolumen() != null) {
            pedido.setVolumen(patchRequest.getVolumen());
        }
        if (patchRequest.getCobertura() != null) {
            pedido.setCobertura(patchRequest.getCobertura());
        }
        if (patchRequest.getDescripcion() != null) {
            pedido.setDescripcion(patchRequest.getDescripcion());
        }
        if (patchRequest.getRepartidorId() != null) {
            pedido.setRepartidorId(patchRequest.getRepartidorId());
        }
        if (patchRequest.getVehiculoId() != null) {
            pedido.setVehiculoId(patchRequest.getVehiculoId());
        }
        if (patchRequest.getFacturaId() != null) {
            pedido.setFacturaId(patchRequest.getFacturaId());
        }
        if (patchRequest.getTarifaCalculada() != null) {
            pedido.setTarifaCalculada(patchRequest.getTarifaCalculada());
        }
        if (patchRequest.getTelefonoContacto() != null) {
            pedido.setTelefonoContacto(patchRequest.getTelefonoContacto());
        }
        if (patchRequest.getNombreDestinatario() != null) {
            pedido.setNombreDestinatario(patchRequest.getNombreDestinatario());
        }
        if (patchRequest.getFechaEstimadaEntrega() != null) {
            pedido.setFechaEstimadaEntrega(patchRequest.getFechaEstimadaEntrega());
        }
    }
}

