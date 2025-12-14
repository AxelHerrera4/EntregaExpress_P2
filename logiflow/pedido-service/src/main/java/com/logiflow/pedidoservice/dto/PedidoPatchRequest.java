package com.logiflow.pedidoservice.dto;

import com.logiflow.pedidoservice.model.Direccion;
import com.logiflow.pedidoservice.model.EstadoPedido;
import com.logiflow.pedidoservice.model.ModalidadServicio;
import com.logiflow.pedidoservice.model.Prioridad;
import com.logiflow.pedidoservice.model.TipoEntrega;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoPatchRequest {

    private Direccion direccionOrigen;
    private Direccion direccionDestino;

    private ModalidadServicio modalidadServicio;
    private TipoEntrega tipoEntrega;
    private EstadoPedido estado;
    private Prioridad prioridad;

    private Double peso;
    private Double volumen;
    private String cobertura;
    private String descripcion;

    // Campos para integración con otros servicios
    private String repartidorId;
    private String vehiculoId;
    private String facturaId;
    private Double tarifaCalculada;

    private String telefonoContacto;
    private String nombreDestinatario;

    private LocalDateTime fechaEstimadaEntrega;
}

