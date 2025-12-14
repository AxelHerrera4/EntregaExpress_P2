package com.logiflow.pedidoservice.dto;

import com.logiflow.pedidoservice.model.Direccion;
import com.logiflow.pedidoservice.model.ModalidadServicio;
import com.logiflow.pedidoservice.model.TipoEntrega;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitud de creación de pedido
 * Patrón: DTO (Data Transfer Object)
 * Aplica validaciones declarativas con Bean Validation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoRequest {

    @NotBlank(message = "El ID del cliente es obligatorio y no puede estar vacío")
    private String clienteId;

    @Valid
    @NotNull(message = "La dirección de origen es obligatoria")
    private Direccion direccionOrigen;

    @Valid
    @NotNull(message = "La dirección de destino es obligatoria")
    private Direccion direccionDestino;

    @NotNull(message = "La modalidad de servicio es obligatoria. Valores válidos: URBANA_RAPIDA, INTERMUNICIPAL, NACIONAL")
    private ModalidadServicio modalidadServicio;

    @NotNull(message = "El tipo de entrega es obligatorio. Valores válidos: EXPRESS, NORMAL, PROGRAMADA")
    private TipoEntrega tipoEntrega;

    @NotNull(message = "El peso es obligatorio")
    @Positive(message = "El peso debe ser un número positivo mayor a 0 (ejemplo: 1.5)")
    private Double peso;

    @NotBlank(message = "El teléfono de contacto es obligatorio")
    @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe contener exactamente 10 dígitos numéricos (ejemplo: 0987654321)")
    private String telefonoContacto;

    private String nombreDestinatario;
}

