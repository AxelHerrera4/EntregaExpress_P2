package com.logiflow.pedidoservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoEstadoEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    // Para idempotencia
    private String messageId;
    private LocalDateTime timestamp;
    
    // Información del evento
    private String pedidoId;
    private String estadoAnterior;
    private String estadoNuevo;
    private String usuarioModificador; // Usuario que modificó el estado
    private String repartidorId;
    private String vehiculoId;
    
    public PedidoEstadoEvent(String pedidoId, String estadoAnterior, String estadoNuevo) {
        this.messageId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.pedidoId = pedidoId;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
    }
    
    public PedidoEstadoEvent(String pedidoId, String estadoAnterior, String estadoNuevo, 
                           String usuarioModificador) {
        this.messageId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.pedidoId = pedidoId;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.usuarioModificador = usuarioModificador;
    }
    
    public PedidoEstadoEvent(String pedidoId, String estadoAnterior, String estadoNuevo, 
                           String usuarioModificador, String repartidorId, String vehiculoId) {
        this.messageId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.pedidoId = pedidoId;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.usuarioModificador = usuarioModificador;
        this.repartidorId = repartidorId;
        this.vehiculoId = vehiculoId;
    }

}