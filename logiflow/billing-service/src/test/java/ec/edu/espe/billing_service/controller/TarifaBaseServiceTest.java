package ec.edu.espe.billing_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.espe.billing_service.model.dto.request.TarifaBaseRequestDTO;
import ec.edu.espe.billing_service.model.dto.response.TarifaBaseResponseDTO;
import ec.edu.espe.billing_service.model.entity.TarifaBase;
import ec.edu.espe.billing_service.service.TarifaBaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class TarifaBaseControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();

        // Servicio fake MANUAL (implementa TODA la interfaz)
        TarifaBaseService tarifaBaseServiceFake = new TarifaBaseService() {

            @Override
            public TarifaBaseResponseDTO crearTarifa(TarifaBaseRequestDTO request) {
                return TarifaBaseResponseDTO.builder()
                        .id(UUID.randomUUID())
                        .tipoEntrega(request.getTipoEntrega().toUpperCase())
                        .tarifaBase(request.getTarifaBase())
                        .build();
            }

            @Override
            public TarifaBaseResponseDTO obtenerPorTipoEntrega(String tipoEntrega) {
                return TarifaBaseResponseDTO.builder()
                        .id(UUID.randomUUID())
                        .tipoEntrega(tipoEntrega.toUpperCase())
                        .tarifaBase(BigDecimal.valueOf(5.00))
                        .build();
            }

            @Override
            public TarifaBaseResponseDTO actualizarTarifa(String tipoEntrega, TarifaBaseRequestDTO request) {
                return TarifaBaseResponseDTO.builder()
                        .id(UUID.randomUUID())
                        .tipoEntrega(tipoEntrega.toUpperCase())
                        .tarifaBase(request.getTarifaBase())
                        .build();
            }

            @Override
            public List<TarifaBaseResponseDTO> obtenerTodasLasTarifas() {
                return List.of(
                        TarifaBaseResponseDTO.builder()
                                .id(UUID.randomUUID())
                                .tipoEntrega("URBANA")
                                .tarifaBase(BigDecimal.valueOf(3.50))
                                .build(),
                        TarifaBaseResponseDTO.builder()
                                .id(UUID.randomUUID())
                                .tipoEntrega("NACIONAL")
                                .tarifaBase(BigDecimal.valueOf(10.00))
                                .build()
                );
            }

            // MÉTODO QUE FALTABA (CLAVE)
            @Override
            public TarifaBase obtenerEntidadPorTipoEntrega(String tipoEntrega) {
                return TarifaBase.builder()
                        .id(UUID.randomUUID())
                        .tipoEntrega(tipoEntrega.toUpperCase())
                        .tarifaBase(BigDecimal.valueOf(5.00))
                        .build();
            }
        };

        // Controller REAL (sin tocar)
        TarifaBaseController controller =
                new TarifaBaseController(tarifaBaseServiceFake);

        // MockMvc en modo standalone
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    /* ===============================
       TEST: Crear tarifa
       =============================== */
    @Test
    void crearTarifa_ok() throws Exception {

        TarifaBaseRequestDTO request = TarifaBaseRequestDTO.builder()
                .tipoEntrega("URBANA")
                .tarifaBase(BigDecimal.valueOf(3.50))
                .build();

        mockMvc.perform(post("/api/tarifas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoEntrega").value("URBANA"));
    }

    /* ===============================
       TEST: Obtener tarifa por tipo
       =============================== */
    @Test
    void obtenerPorTipoEntrega_ok() throws Exception {

        mockMvc.perform(get("/api/tarifas/{tipoEntrega}", "URBANA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoEntrega").value("URBANA"));
    }

    /* ===============================
       TEST: Actualizar tarifa
       =============================== */
    @Test
    void actualizarTarifa_ok() throws Exception {

        TarifaBaseRequestDTO request = TarifaBaseRequestDTO.builder()
                .tipoEntrega("URBANA")
                .tarifaBase(BigDecimal.valueOf(4.00))
                .build();

        mockMvc.perform(put("/api/tarifas/{tipoEntrega}", "URBANA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tarifaBase").value(4.00));
    }

    /* ===============================
       TEST: Listar todas las tarifas
       =============================== */
    @Test
    void verTodasLasTarifas_ok() throws Exception {

        mockMvc.perform(get("/api/tarifas/tarifas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
