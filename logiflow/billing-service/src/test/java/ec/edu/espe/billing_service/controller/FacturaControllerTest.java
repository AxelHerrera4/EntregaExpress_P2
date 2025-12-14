package ec.edu.espe.billing_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ec.edu.espe.billing_service.model.dto.request.FacturaRequestDTO;
import ec.edu.espe.billing_service.model.dto.response.FacturaResponseDTO;
import ec.edu.espe.billing_service.model.enums.EstadoFactura;
import ec.edu.espe.billing_service.service.FacturaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class FacturaControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Servicio fake manual (implementación directa)
        FacturaService facturaServiceFake = new FacturaService() {

            @Override
            public FacturaResponseDTO crearFactura(FacturaRequestDTO request) {
                return FacturaResponseDTO.builder()
                        .id(UUID.randomUUID())
                        .pedidoId(request.getPedidoId())
                        .tipoEntrega(request.getTipoEntrega())
                        .montoTotal(BigDecimal.valueOf(15.00))
                        .estado(EstadoFactura.BORRADOR)
                        .fechaCreacion(LocalDateTime.now())
                        .distanciaKm(request.getDistanciaKm())
                        .build();
            }

            @Override
            public FacturaResponseDTO obtenerFacturaPorId(UUID facturaId) {
                return FacturaResponseDTO.builder()
                        .id(facturaId)
                        .pedidoId("PED-001")
                        .tipoEntrega("URBANA")
                        .montoTotal(BigDecimal.valueOf(15.00))
                        .estado(EstadoFactura.BORRADOR)
                        .fechaCreacion(LocalDateTime.now())
                        .distanciaKm(10.0)
                        .build();
            }

            @Override
            public FacturaResponseDTO obtenerFacturaPorPedidoId(String pedidoId) {
                return FacturaResponseDTO.builder()
                        .id(UUID.randomUUID())
                        .pedidoId(pedidoId)
                        .tipoEntrega("NACIONAL")
                        .montoTotal(BigDecimal.valueOf(100.00))
                        .estado(EstadoFactura.BORRADOR)
                        .fechaCreacion(LocalDateTime.now())
                        .distanciaKm(200.0)
                        .build();
            }

            @Override
            public FacturaResponseDTO actualizarEstado(UUID facturaId, EstadoFactura estado) {
                return FacturaResponseDTO.builder()
                        .id(facturaId)
                        .pedidoId("PED-010")
                        .tipoEntrega("URBANA")
                        .montoTotal(BigDecimal.valueOf(12.00))
                        .estado(estado)
                        .fechaCreacion(LocalDateTime.now())
                        .distanciaKm(5.0)
                        .build();
            }
        };

        // Controller REAL (tal como lo tienes)
        FacturaController controller = new FacturaController(facturaServiceFake);

        // MockMvc standalone
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    /* ===============================
       TEST: Crear factura
       =============================== */
    @Test
    void crearFactura_ok() throws Exception {

        FacturaRequestDTO request = FacturaRequestDTO.builder()
                .pedidoId("PED-001")
                .tipoEntrega("URBANA")
                .distanciaKm(10.0)
                .build();

        mockMvc.perform(post("/api/facturas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("BORRADOR"));
    }

    /* ===============================
       TEST: Obtener factura por ID
       =============================== */
    @Test
    void obtenerFacturaPorId_ok() throws Exception {

        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/api/facturas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    /* ===============================
       TEST: Obtener factura por pedido
       =============================== */
    @Test
    void obtenerFacturaPorPedidoId_ok() throws Exception {

        String pedidoId = "PED-005";

        mockMvc.perform(get("/api/facturas/pedido/{pedidoId}", pedidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidoId").value(pedidoId));
    }

    /* ===============================
       TEST: Actualizar estado
       =============================== */
    @Test
    void actualizarEstado_ok() throws Exception {

        UUID id = UUID.randomUUID();

        mockMvc.perform(patch("/api/facturas/{id}/estado", id)
                        .param("estado", "PAGADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PAGADA"));
    }
}
