package com.logiflow.pedidoservice.dto;

import com.logiflow.pedidoservice.model.Direccion;
import com.logiflow.pedidoservice.model.EstadoPedido;
import com.logiflow.pedidoservice.model.ModalidadServicio;
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
public class PedidoResponse {

    private String id;
    private String clienteId;

    private Direccion direccionOrigen;
    private Direccion direccionDestino;

    private ModalidadServicio modalidadServicio;
    private TipoEntrega tipoEntrega;
    private EstadoPedido estado;

    private Double peso;
    private String telefonoContacto;
    private String nombreDestinatario;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String cobertura;
}
