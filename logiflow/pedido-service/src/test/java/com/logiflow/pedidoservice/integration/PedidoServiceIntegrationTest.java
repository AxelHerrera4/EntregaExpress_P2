package com.logiflow.pedidoservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logiflow.pedidoservice.dto.PedidoRequest;
import com.logiflow.pedidoservice.dto.PedidoResponse;
import com.logiflow.pedidoservice.model.*;
import com.logiflow.pedidoservice.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests de integración para PedidoService")
class PedidoServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private PedidoRequest pedidoRequest;
    private Direccion direccionOrigen;
    private Direccion direccionDestino;

    @BeforeEach
    void setUp() {
        // Limpiar repositorio antes de cada test
        pedidoRepository.deleteAll();

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
    }

    @Test
    @WithMockUser
    @DisplayName("Flujo completo: crear, obtener, actualizar y eliminar pedido")
    void flujoCompletoCRUD() throws Exception {
        // 1. CREAR PEDIDO
        String responseContent = mockMvc.perform(post("/api/pedidos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clienteId").value("cli-12345"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.modalidadServicio").value("NACIONAL"))
                .andExpect(jsonPath("$.tipoEntrega").value("EXPRESS"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        PedidoResponse pedidoCreado = objectMapper.readValue(responseContent, PedidoResponse.class);
        System.out.println("RESPONSE JSON: " + responseContent);
        // Aserción sobre el DTO (diagnóstico)
        assertEquals("NACIONAL", pedidoCreado.getCobertura());
        String pedidoId = pedidoCreado.getId();
        assertNotNull(pedidoId);

        // Verificar que se guardó en la base de datos
        assertTrue(pedidoRepository.findById(pedidoId).isPresent());
        assertEquals(1, pedidoRepository.count());

        // 2. OBTENER PEDIDO POR ID
        mockMvc.perform(get("/api/pedidos/{id}", pedidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pedidoId))
                .andExpect(jsonPath("$.clienteId").value("cli-12345"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        // 3. LISTAR TODOS LOS PEDIDOS
        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(pedidoId));

        // 4. OBTENER PEDIDOS POR CLIENTE
        mockMvc.perform(get("/api/pedidos/cliente/{clienteId}", "cli-12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].clienteId").value("cli-12345"));
    }
}
