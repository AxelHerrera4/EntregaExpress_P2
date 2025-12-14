package ec.edu.espe.billing_service.service.impl;

import ec.edu.espe.billing_service.model.dto.request.TarifaBaseRequestDTO;
import ec.edu.espe.billing_service.model.dto.response.TarifaBaseResponseDTO;
import ec.edu.espe.billing_service.model.entity.TarifaBase;
import ec.edu.espe.billing_service.repository.TarifaBaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TarifaBaseServiceImplTest {

    @Mock
    private TarifaBaseRepository tarifaBaseRepository;

    @InjectMocks
    private TarifaBaseServiceImpl tarifaBaseService;

    private TarifaBase tarifaBase;

    @BeforeEach
    void setup() {
        tarifaBase = TarifaBase.builder()
                .id(UUID.randomUUID())
                .tipoEntrega("URBANA")
                .tarifaBase(new BigDecimal("5.00"))
                .build();
    }

    /* =========================
       crearTarifa
       ========================= */

    @Test
    void crearTarifa_ok() {
        TarifaBaseRequestDTO request = TarifaBaseRequestDTO.builder()
                .tipoEntrega("urbana")
                .tarifaBase(new BigDecimal("5.00"))
                .build();

        when(tarifaBaseRepository.existsByTipoEntrega("URBANA")).thenReturn(false);
        when(tarifaBaseRepository.save(any(TarifaBase.class))).thenReturn(tarifaBase);

        TarifaBaseResponseDTO response = tarifaBaseService.crearTarifa(request);

        assertNotNull(response);
        assertEquals("URBANA", response.getTipoEntrega());
        assertEquals(new BigDecimal("5.00"), response.getTarifaBase());

        verify(tarifaBaseRepository).save(any(TarifaBase.class));
    }

    @Test
    void crearTarifa_duplicada_lanzaExcepcion() {
        TarifaBaseRequestDTO request = TarifaBaseRequestDTO.builder()
                .tipoEntrega("URBANA")
                .tarifaBase(new BigDecimal("5.00"))
                .build();

        when(tarifaBaseRepository.existsByTipoEntrega("URBANA")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> tarifaBaseService.crearTarifa(request));

        assertTrue(ex.getMessage().contains("Ya existe una tarifa"));
    }

    /* =========================
       obtenerPorTipoEntrega
       ========================= */

    @Test
    void obtenerPorTipoEntrega_ok() {
        when(tarifaBaseRepository.findByTipoEntrega("URBANA"))
                .thenReturn(Optional.of(tarifaBase));

        TarifaBaseResponseDTO response =
                tarifaBaseService.obtenerPorTipoEntrega("urbana");

        assertEquals("URBANA", response.getTipoEntrega());
    }

    @Test
    void obtenerPorTipoEntrega_noExiste_lanzaExcepcion() {
        when(tarifaBaseRepository.findByTipoEntrega("URBANA"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> tarifaBaseService.obtenerPorTipoEntrega("urbana"));

        assertTrue(ex.getMessage().contains("No existe tarifa"));
    }

    /* =========================
       actualizarTarifa
       ========================= */

    @Test
    void actualizarTarifa_ok() {
        TarifaBaseRequestDTO request = TarifaBaseRequestDTO.builder()
                .tarifaBase(new BigDecimal("8.00"))
                .build();

        when(tarifaBaseRepository.findByTipoEntrega("URBANA"))
                .thenReturn(Optional.of(tarifaBase));
        when(tarifaBaseRepository.save(any(TarifaBase.class)))
                .thenReturn(tarifaBase);

        TarifaBaseResponseDTO response =
                tarifaBaseService.actualizarTarifa("urbana", request);

        assertEquals(new BigDecimal("8.00"), response.getTarifaBase());
    }

    @Test
    void actualizarTarifa_noExiste_lanzaExcepcion() {
        TarifaBaseRequestDTO request = TarifaBaseRequestDTO.builder()
                .tarifaBase(new BigDecimal("10.00"))
                .build();

        when(tarifaBaseRepository.findByTipoEntrega("URBANA"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> tarifaBaseService.actualizarTarifa("urbana", request));

        assertTrue(ex.getMessage().contains("No existe tarifa"));
    }

    /* =========================
       obtenerEntidadPorTipoEntrega
       ========================= */

    @Test
    void obtenerEntidadPorTipoEntrega_ok() {
        when(tarifaBaseRepository.findByTipoEntrega("URBANA"))
                .thenReturn(Optional.of(tarifaBase));

        TarifaBase result =
                tarifaBaseService.obtenerEntidadPorTipoEntrega("urbana");

        assertNotNull(result);
        assertEquals("URBANA", result.getTipoEntrega());
    }

    @Test
    void obtenerEntidadPorTipoEntrega_noExiste_lanzaExcepcion() {
        when(tarifaBaseRepository.findByTipoEntrega("URBANA"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> tarifaBaseService.obtenerEntidadPorTipoEntrega("urbana"));

        assertTrue(ex.getMessage().contains("No existe tarifa"));
    }

    /* =========================
       obtenerTodasLasTarifas
       ========================= */

    @Test
    void obtenerTodasLasTarifas_ok() {
        when(tarifaBaseRepository.findAll())
                .thenReturn(List.of(tarifaBase));

        List<TarifaBaseResponseDTO> result =
                tarifaBaseService.obtenerTodasLasTarifas();

        assertEquals(1, result.size());
    }

    /* =========================
       normalizeTipoEntrega
       ========================= */

    @Test
    void normalizeTipoEntrega_null_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> tarifaBaseService.obtenerEntidadPorTipoEntrega(null));
    }

    @Test
    void normalizeTipoEntrega_vacio_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> tarifaBaseService.obtenerEntidadPorTipoEntrega("   "));
    }
}
