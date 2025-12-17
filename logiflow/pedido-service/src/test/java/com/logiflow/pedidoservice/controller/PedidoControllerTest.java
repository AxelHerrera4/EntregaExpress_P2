package com.logiflow.pedidoservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logiflow.pedidoservice.dto.PedidoRequest;
import com.logiflow.pedidoservice.dto.PedidoResponse;
import com.logiflow.pedidoservice.model.*;
import com.logiflow.pedidoservice.service.PedidoService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PedidoController.class)
@DisplayName("Tests unitarios para PedidoController")
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoService pedidoService;

    @Autowired
    private ObjectMapper objectMapper;

    private PedidoRequest pedidoRequest;
    private PedidoResponse pedidoResponse;
    private Direccion direccionOrigen;
    private Direccion direccionDestino;

    @BeforeEach
    void setUp() {
        direccionOrigen = Direccion.builder()
                .calle("Av Principal")
                .numero("123")
                .ciudad("Quito")
                .provincia("Pichincha")
                .build();

        direccionDestino = Direccion.builder()
                .calle("Calle Secundaria")
                .numero("456")
                .ciudad("Guayaquil")
                .provincia("Guayas")
                .build();

        pedidoRequest = PedidoRequest.builder()
                .clienteId("cli-12345")
                .direccionOrigen(direccionOrigen)
                .direccionDestino(direccionDestino)
                .modalidadServicio(ModalidadServicio.NACIONAL)
                .tipoEntrega(TipoEntrega.EXPRESS)
                .peso(2.5)
                .telefonoContacto("0987654321")
                .nombreDestinatario("Juan Pérez")
                .build();

        pedidoResponse = PedidoResponse.builder()
                .id("ped-123")
                .clienteId("cli-12345")
                .direccionOrigen(direccionOrigen)
                .direccionDestino(direccionDestino)
                .modalidadServicio(ModalidadServicio.NACIONAL)
                .tipoEntrega(TipoEntrega.EXPRESS)
                .estado(EstadoPedido.PENDIENTE)
                .peso(2.5)
                .telefonoContacto("0987654321")
                .nombreDestinatario("Juan Pérez")
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/pedidos - Crear pedido exitosamente")
    void crearPedido_Exitoso() throws Exception {
        // Given
        when(pedidoService.createPedido(any(PedidoRequest.class))).thenReturn(pedidoResponse);

        // When & Then
        mockMvc.perform(post("/api/pedidos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("ped-123"))
                .andExpect(jsonPath("$.clienteId").value("cli-12345"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.modalidadServicio").value("NACIONAL"))
                .andExpect(jsonPath("$.tipoEntrega").value("EXPRESS"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/pedidos - Request inválido")
    void crearPedido_RequestInvalido() throws Exception {
        // Given - Request sin clienteId
        PedidoRequest requestInvalido = PedidoRequest.builder()
                .direccionOrigen(direccionOrigen)
                .direccionDestino(direccionDestino)
                .modalidadServicio(ModalidadServicio.NACIONAL)
                .tipoEntrega(TipoEntrega.EXPRESS)
                .peso(2.5)
                .build();

        // When & Then
        mockMvc.perform(post("/api/pedidos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/pedidos/{id} - Obtener pedido exitosamente")
    void obtenerPedidoPorId_Exitoso() throws Exception {
        // Given
        String pedidoId = "ped-123";
        when(pedidoService.getPedidoById(pedidoId)).thenReturn(pedidoResponse);

        // When & Then
        mockMvc.perform(get("/api/pedidos/{id}", pedidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pedidoId))
                .andExpect(jsonPath("$.clienteId").value("cli-12345"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/pedidos/{id} - Pedido no encontrado")
    void obtenerPedidoPorId_NoEncontrado() throws Exception {
        // Given
        String pedidoId = "ped-inexistente";
        when(pedidoService.getPedidoById(pedidoId))
                .thenThrow(new EntityNotFoundException("Pedido no encontrado con ID: " + pedidoId));

        // When & Then
        mockMvc.perform(get("/api/pedidos/{id}", pedidoId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/pedidos - Obtener todos los pedidos")
    void obtenerTodosPedidos_Exitoso() throws Exception {
        // Given
        List<PedidoResponse> pedidos = List.of(pedidoResponse);
        when(pedidoService.getAllPedidos()).thenReturn(pedidos);

        // When & Then
        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("ped-123"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/pedidos/cliente/{clienteId} - Obtener pedidos por cliente")
    void obtenerPedidosPorCliente_Exitoso() throws Exception {
        // Given
        String clienteId = "cli-12345";
        List<PedidoResponse> pedidos = List.of(pedidoResponse);
        when(pedidoService.getPedidosByCliente(clienteId)).thenReturn(pedidos);

        // When & Then
        mockMvc.perform(get("/api/pedidos/cliente/{clienteId}", clienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].clienteId").value(clienteId));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/pedidos/{id} - Eliminar pedido exitosamente")
    void eliminarPedido_Exitoso() throws Exception {
        // Given
        String pedidoId = "ped-123";

        // When & Then
        mockMvc.perform(delete("/api/pedidos/{id}", pedidoId)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/pedidos/{id} - Pedido no encontrado")
    void eliminarPedido_NoEncontrado() throws Exception {
        // Given
        String pedidoId = "ped-inexistente";
        doThrow(new EntityNotFoundException("Pedido no encontrado con ID: " + pedidoId))
                .when(pedidoService).deletePedido(pedidoId);

        // When & Then
        mockMvc.perform(delete("/api/pedidos/{id}", pedidoId)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
