package ec.edu.espe.billing_service.service.impl;

import ec.edu.espe.billing_service.factory.TarifaStrategyFactory;
import ec.edu.espe.billing_service.model.dto.request.FacturaRequestDTO;
import ec.edu.espe.billing_service.model.dto.response.FacturaResponseDTO;
import ec.edu.espe.billing_service.model.entity.Factura;
import ec.edu.espe.billing_service.model.entity.TarifaBase;
import ec.edu.espe.billing_service.model.enums.EstadoFactura;
import ec.edu.espe.billing_service.repository.FacturaRepository;
import ec.edu.espe.billing_service.service.TarifaBaseService;
import ec.edu.espe.billing_service.strategy.TarifaStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class FacturaServiceImplTest {

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private TarifaBaseService tarifaBaseService;

    @Mock
    private TarifaStrategyFactory tarifaStrategyFactory;

    @Mock
    private TarifaStrategy tarifaStrategy;

    @InjectMocks
    private FacturaServiceImpl facturaService;

    private FacturaRequestDTO request;

    @BeforeEach
    void setup() {
        request = FacturaRequestDTO.builder()
                .pedidoId("PED-001")
                .tipoEntrega("URBANA")
                .distanciaKm(10.0)
                .build();
    }



    @Test
    void crearFactura_ok() {

        TarifaBase tarifaBase = TarifaBase.builder()
                .tipoEntrega("URBANA")
                .tarifaBase(BigDecimal.valueOf(5.0))
                .build();

        when(facturaRepository.existsByPedidoId("PED-001")).thenReturn(false);
        when(tarifaBaseService.obtenerEntidadPorTipoEntrega("URBANA")).thenReturn(tarifaBase);
        when(tarifaStrategyFactory.obtenerStrategy("URBANA")).thenReturn(tarifaStrategy);
        when(tarifaStrategy.calcularTarifa(tarifaBase, 10.0))
                .thenReturn(BigDecimal.valueOf(50));

        when(facturaRepository.save(any(Factura.class)))
                .thenAnswer(invocation -> {
                    Factura f = invocation.getArgument(0);
                    f.setId(UUID.randomUUID());
                    f.setFechaCreacion(LocalDateTime.now());
                    return f;
                });

        FacturaResponseDTO response = facturaService.crearFactura(request);

        assertNotNull(response);
        assertEquals(EstadoFactura.BORRADOR, response.getEstado());
        assertEquals(0, response.getMontoTotal().compareTo(BigDecimal.valueOf(50)));
    }

    @Test
    void crearFactura_pedidoDuplicado_lanzaExcepcion() {

        when(facturaRepository.existsByPedidoId("PED-001")).thenReturn(true);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> facturaService.crearFactura(request)
        );

        assertTrue(ex.getMessage().contains("Ya existe una factura"));
    }



    @Test
    void obtenerFacturaPorId_ok() {

        UUID id = UUID.randomUUID();

        Factura factura = Factura.builder()
                .id(id)
                .pedidoId("PED-001")
                .tipoEntrega("URBANA")
                .montoTotal(BigDecimal.TEN)
                .estado(EstadoFactura.BORRADOR)
                .fechaCreacion(LocalDateTime.now())
                .distanciaKm(10.0)
                .build();

        when(facturaRepository.findById(id))
                .thenReturn(Optional.of(factura));

        FacturaResponseDTO response = facturaService.obtenerFacturaPorId(id);

        assertEquals(id, response.getId());
    }

    @Test
    void obtenerFacturaPorId_noExiste_lanzaExcepcion() {

        UUID id = UUID.randomUUID();

        when(facturaRepository.findById(id))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> facturaService.obtenerFacturaPorId(id)
        );

        assertEquals("Factura no encontrada", ex.getMessage());
    }



    @Test
    void obtenerFacturaPorPedidoId_ok() {

        Factura factura = Factura.builder()
                .id(UUID.randomUUID())
                .pedidoId("PED-005")
                .tipoEntrega("URBANA")
                .montoTotal(BigDecimal.TEN)
                .estado(EstadoFactura.BORRADOR)
                .fechaCreacion(LocalDateTime.now())
                .distanciaKm(10.0)
                .build();

        when(facturaRepository.findByPedidoId("PED-005"))
                .thenReturn(Optional.of(factura));

        FacturaResponseDTO response =
                facturaService.obtenerFacturaPorPedidoId("PED-005");

        assertEquals("PED-005", response.getPedidoId());
    }

    @Test
    void obtenerFacturaPorPedidoId_noExiste_lanzaExcepcion() {

        when(facturaRepository.findByPedidoId("PED-099"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> facturaService.obtenerFacturaPorPedidoId("PED-099")
        );

        assertTrue(ex.getMessage().contains("Factura no encontrada"));
    }



    @Test
    void actualizarEstado_ok() {

        UUID id = UUID.randomUUID();

        Factura factura = Factura.builder()
                .id(id)
                .pedidoId("PED-001")
                .estado(EstadoFactura.BORRADOR)
                .build();

        when(facturaRepository.findById(id))
                .thenReturn(Optional.of(factura));

        when(facturaRepository.save(any(Factura.class)))
                .thenReturn(factura);

        FacturaResponseDTO response =
                facturaService.actualizarEstado(id, EstadoFactura.PAGADA);

        assertEquals(EstadoFactura.PAGADA, response.getEstado());
    }

    @Test
    void actualizarEstado_facturaNoExiste_lanzaExcepcion() {

        UUID id = UUID.randomUUID();

        when(facturaRepository.findById(id))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> facturaService.actualizarEstado(id, EstadoFactura.PAGADA)
        );

        assertEquals("Factura no encontrada", ex.getMessage());
    }
}
