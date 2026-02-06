package com.logiflow.pedidoservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoCreadoEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    // Para idempotencia
    private String messageId;
    private LocalDateTime timestamp;
    
    // Información del pedido
    private String pedidoId;
    private String clienteId;
    private String usuarioCreador; // Usuario que creó el pedido
    private String estado;
    private String tipoEntrega;
    private String modalidadServicio;
    private String prioridad;
    private Double peso;
    private String direccionOrigen;
    private String direccionDestino;
    private String ciudadOrigen;
    private String ciudadDestino;
    private Double distanciaEstimadaKm;
    private BigDecimal tarifaCalculada;
    
    public PedidoCreadoEvent(String pedidoId, String clienteId, String usuarioCreador, String estado, 
                           String tipoEntrega, String modalidadServicio, String prioridad, Double peso,
                           String direccionOrigen, String direccionDestino, String ciudadOrigen, 
                           String ciudadDestino, Double distanciaEstimadaKm, BigDecimal tarifaCalculada) {
        this.messageId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.pedidoId = pedidoId;
        this.clienteId = clienteId;
        this.usuarioCreador = usuarioCreador;
        this.estado = estado;
        this.tipoEntrega = tipoEntrega;
        this.modalidadServicio = modalidadServicio;
        this.prioridad = prioridad;
        this.peso = peso;
        this.direccionOrigen = direccionOrigen;
        this.direccionDestino = direccionDestino;
        this.ciudadOrigen = ciudadOrigen;
        this.ciudadDestino = ciudadDestino;
        this.distanciaEstimadaKm = distanciaEstimadaKm;
        this.tarifaCalculada = tarifaCalculada;
    }
}