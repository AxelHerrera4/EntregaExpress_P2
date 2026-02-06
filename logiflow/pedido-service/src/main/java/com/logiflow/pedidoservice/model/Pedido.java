package com.logiflow.pedidoservice.model;


import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name= "pedidos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank(message = "El ID del cliente es obligatorio")
    @Column(nullable = false)
    private String clienteId;

    // Direcciones embebidas para soportar las 3 modalidades de entrega
    @Valid
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "calle", column = @Column(name = "origen_calle")),
        @AttributeOverride(name = "numero", column = @Column(name = "origen_numero")),
        @AttributeOverride(name = "ciudad", column = @Column(name = "origen_ciudad")),
        @AttributeOverride(name = "provincia", column = @Column(name = "origen_provincia")),
            @AttributeOverride(name = "latitud", column = @Column(name = "origen_latitud")),
            @AttributeOverride(name = "longitud", column = @Column(name = "origen_longitud"))
    })
    private Direccion direccionOrigen;

    @Valid
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "calle", column = @Column(name = "destino_calle")),
        @AttributeOverride(name = "numero", column = @Column(name = "destino_numero")),
        @AttributeOverride(name = "ciudad", column = @Column(name = "destino_ciudad")),
        @AttributeOverride(name = "provincia", column = @Column(name = "destino_provincia")),
            @AttributeOverride(name = "latitud", column = @Column(name = "destino_latitud")),
            @AttributeOverride(name = "longitud", column = @Column(name = "destino_longitud"))
    })
    private Direccion direccionDestino;

    @NotNull(message = "La modalidad de servicio es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModalidadServicio modalidadServicio;

    @NotNull(message = "El tipo de entrega es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEntrega tipoEntrega;

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado;

    @Positive(message = "El peso debe ser mayor a 0")
    @Column(nullable = false)
    private Double peso; // en kilogramos

    @Positive(message = "El volumen debe ser mayor a 0")
    private Double volumen; // en metros cúbicos (opcional para cálculo de tarifa)

    @Column(nullable = false)
    private String cobertura; // Zona geográfica

    private String descripcion;

    // Integración con FleetService
    private String repartidorId; // ID del repartidor asignado
    private String vehiculoId;   // ID del vehículo asignado

    // Integración con BillingService
    private String facturaId;    // ID de la factura generada
    private Double tarifaCalculada; // Tarifa calculada por BillingService

    // Información de contacto
    @Column(nullable = false)
    private String telefonoContacto;

    private String nombreDestinatario;

    // Fechas y horarios
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;

    private LocalDateTime fechaEstimadaEntrega;

    private LocalDateTime fechaEntregaReal;

    // Prioridad del pedido (para gestión de fleet)
    @Enumerated(EnumType.STRING)
    private Prioridad prioridad;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (estado == null) {
            estado = EstadoPedido.PENDIENTE;
        }
        if (prioridad == null) {
            prioridad = tipoEntrega == TipoEntrega.EXPRESS
                ? Prioridad.ALTA
                : Prioridad.NORMAL;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
        if (estado == EstadoPedido.ENTREGADO && fechaEntregaReal == null) {
            fechaEntregaReal = LocalDateTime.now();
        }
    }
}
