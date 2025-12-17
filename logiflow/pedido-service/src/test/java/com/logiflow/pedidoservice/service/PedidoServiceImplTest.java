package com.logiflow.pedidoservice.service;

import com.logiflow.pedidoservice.client.BillingClient;
import com.logiflow.pedidoservice.client.FleetClient;
import com.logiflow.pedidoservice.dto.PedidoMapper;
import com.logiflow.pedidoservice.dto.PedidoRequest;
import com.logiflow.pedidoservice.dto.PedidoResponse;
import com.logiflow.pedidoservice.dto.PedidoPatchRequest;
import com.logiflow.pedidoservice.model.*;
import com.logiflow.pedidoservice.repository.PedidoRepository;
import com.logiflow.pedidoservice.service.impl.PedidoServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios para PedidoServiceImpl")
class PedidoServiceImplTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private PedidoMapper pedidoMapper;

    @Mock
    private CoberturaValidationService coberturaValidationService;

    @Mock
    private BillingClient billingClient;

    @Mock
    private FleetClient fleetClient;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    private PedidoRequest pedidoRequest;
    private Pedido pedido;
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

        pedido = Pedido.builder()
                .id("ped-123")
                .clienteId("cli-12345")
                .direccionOrigen(direccionOrigen)
                .direccionDestino(direccionDestino)
                .modalidadServicio(ModalidadServicio.NACIONAL)
                .tipoEntrega(TipoEntrega.EXPRESS)
                .estado(EstadoPedido.PENDIENTE)
                .cobertura("NACIONAL")
                .peso(2.5)
                .telefonoContacto("0987654321")
                .nombreDestinatario("Juan Pérez")
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
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
    @DisplayName("Crear pedido exitosamente")
    void crearPedido_Exitoso() {
        // Given
        when(coberturaValidationService.isValidCobertura("NACIONAL")).thenReturn(true);
        when(coberturaValidationService.isTipoEntregaDisponible(any(TipoEntrega.class), anyString())).thenReturn(true);

        when(pedidoMapper.toEntity(any(PedidoRequest.class))).thenReturn(pedido);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        when(pedidoMapper.toResponse(any(Pedido.class))).thenReturn(pedidoResponse);

        // When
        PedidoResponse result = pedidoService.createPedido(pedidoRequest);

        // Then
        assertNotNull(result);
        assertEquals("ped-123", result.getId());
        assertEquals("cli-12345", result.getClienteId());
        assertEquals(EstadoPedido.PENDIENTE, result.getEstado());

        verify(coberturaValidationService).isValidCobertura("NACIONAL");
        verify(coberturaValidationService).isTipoEntregaDisponible(any(TipoEntrega.class), anyString());
        verify(pedidoRepository).save(any(Pedido.class));
        verify(pedidoMapper).toEntity(pedidoRequest);
        verify(pedidoMapper).toResponse(pedido);
    }

    @Test
    @DisplayName("Obtener pedido por ID exitosamente")
    void obtenerPedidoPorId_Exitoso() {
        // Given
        String pedidoId = "ped-123";
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        when(pedidoMapper.toResponse(pedido)).thenReturn(pedidoResponse);

        // When
        PedidoResponse result = pedidoService.getPedidoById(pedidoId);

        // Then
        assertNotNull(result);
        assertEquals(pedidoId, result.getId());
        assertEquals("cli-12345", result.getClienteId());

        verify(pedidoRepository).findById(pedidoId);
        verify(pedidoMapper).toResponse(pedido);
    }

    @Test
    @DisplayName("Obtener pedido por ID - No encontrado")
    void obtenerPedidoPorId_NoEncontrado() {
        // Given
        String pedidoId = "ped-inexistente";
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> pedidoService.getPedidoById(pedidoId)
        );

        assertEquals("Pedido no encontrado con ID: " + pedidoId, exception.getMessage());
        verify(pedidoRepository).findById(pedidoId);
        verify(pedidoMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Obtener todos los pedidos exitosamente")
    void obtenerTodosPedidos_Exitoso() {
        // Given
        List<Pedido> pedidos = List.of(pedido);
        when(pedidoRepository.findAll()).thenReturn(pedidos);
        when(pedidoMapper.toResponse(pedido)).thenReturn(pedidoResponse);

        // When
        List<PedidoResponse> result = pedidoService.getAllPedidos();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ped-123", result.get(0).getId());

        verify(pedidoRepository).findAll();
        verify(pedidoMapper).toResponse(pedido);
    }

    @Test
    @DisplayName("Obtener pedidos por cliente exitosamente")
    void obtenerPedidosPorCliente_Exitoso() {
        // Given
        String clienteId = "cli-12345";
        List<Pedido> pedidos = List.of(pedido);
        when(pedidoRepository.findByClienteId(clienteId)).thenReturn(pedidos);
        when(pedidoMapper.toResponse(pedido)).thenReturn(pedidoResponse);

        // When
        List<PedidoResponse> result = pedidoService.getPedidosByCliente(clienteId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(clienteId, result.get(0).getClienteId());

        verify(pedidoRepository).findByClienteId(clienteId);
        verify(pedidoMapper).toResponse(pedido);
    }

    @Test
    @DisplayName("Eliminar pedido exitosamente")
    void eliminarPedido_Exitoso() {
        // Given
        String pedidoId = "ped-123";
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

        // When
        assertDoesNotThrow(() -> pedidoService.deletePedido(pedidoId));

        // Then
        verify(pedidoRepository).findById(pedidoId);
        verify(pedidoRepository).delete(pedido);
    }

    @Test
    @DisplayName("Eliminar pedido - No encontrado")
    void eliminarPedido_NoEncontrado() {
        // Given
        String pedidoId = "ped-inexistente";
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> pedidoService.deletePedido(pedidoId)
        );

        assertEquals("Pedido no encontrado con ID: " + pedidoId, exception.getMessage());
        verify(pedidoRepository).findById(pedidoId);
        verify(pedidoRepository, never()).delete(any(com.logiflow.pedidoservice.model.Pedido.class));
    }

    // ------------------------------------------------------------------
    // Nuevos tests agregados para cubrir más métodos de PedidoServiceImpl
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Patch pedido exitoso")
    void patchPedido_Exitoso() {
        // Given
        String pedidoId = "ped-123";
        PedidoPatchRequest patch = PedidoPatchRequest.builder()
                .telefonoContacto("0999999999")
                .build();

        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        // El mapper actualiza entidad in-place, no devuelve nada; simulamos que hace la actualización
        doAnswer(invocation -> {
            Pedido target = invocation.getArgument(0);
            PedidoPatchRequest req = invocation.getArgument(1);
            target.setTelefonoContacto(req.getTelefonoContacto());
            return null;
        }).when(pedidoMapper).updateEntityFromPatch(any(Pedido.class), any(PedidoPatchRequest.class));

        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        when(pedidoMapper.toResponse(any(Pedido.class))).thenReturn(pedidoResponse);

        // When
        PedidoResponse result = pedidoService.patchPedido(pedidoId, patch);

        // Then
        assertNotNull(result);
        verify(pedidoRepository).findById(pedidoId);
        verify(pedidoMapper).updateEntityFromPatch(any(Pedido.class), any(PedidoPatchRequest.class));
        verify(pedidoRepository).save(pedido);
        verify(pedidoMapper).toResponse(pedido);
    }

    @Test
    @DisplayName("Patch pedido - no permite modificar pedido cancelado")
    void patchPedido_NoPermiteModificacionPorEstado() {
        // Given
        String pedidoId = "ped-123";
        pedido.setEstado(EstadoPedido.CANCELADO);
        PedidoPatchRequest patch = PedidoPatchRequest.builder().telefonoContacto("0999").build();

        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

        // When & Then
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> pedidoService.patchPedido(pedidoId, patch));
        assertEquals("No se puede modificar un pedido cancelado", ex.getMessage());
        verify(pedidoRepository).findById(pedidoId);
    }

    @Test
    @DisplayName("Cancelar pedido exitosamente")
    void cancelarPedido_Exitoso() {
        // Given
        String pedidoId = "ped-123";
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        when(pedidoMapper.toResponse(any(Pedido.class))).thenReturn(pedidoResponse);

        // When
        PedidoResponse result = pedidoService.cancelarPedido(pedidoId);

        // Then
        assertNotNull(result);
        verify(pedidoRepository).findById(pedidoId);
        verify(pedidoRepository).save(any(Pedido.class));
        verify(pedidoMapper).toResponse(any(Pedido.class));
    }

    @Test
    @DisplayName("Cancelar pedido - no permite si entregado")
    void cancelarPedido_NoPermiteSiEntregado() {
        // Given
        String pedidoId = "ped-123";
        pedido.setEstado(EstadoPedido.ENTREGADO);
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

        // When & Then
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> pedidoService.cancelarPedido(pedidoId));
        assertEquals("No se puede cancelar un pedido ya entregado", ex.getMessage());
        verify(pedidoRepository).findById(pedidoId);
    }

    @Test
    @DisplayName("Asignar repartidor y vehiculo exitosamente")
    void asignarRepartidorYVehiculo_Exitoso() {
        // Given
        String pedidoId = "ped-123";
        String repartidorId = "rep-1";
        String vehiculoId = "veh-1";
        pedido.setEstado(EstadoPedido.PENDIENTE);

        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        when(pedidoMapper.toResponse(any(Pedido.class))).thenReturn(pedidoResponse);

        // When
        PedidoResponse result = pedidoService.asignarRepartidorYVehiculo(pedidoId, repartidorId, vehiculoId);

        // Then
        assertNotNull(result);
        verify(pedidoRepository).findById(pedidoId);
        verify(pedidoRepository).save(any(Pedido.class));
        verify(pedidoMapper).toResponse(any(Pedido.class));
    }

    @Test
    @DisplayName("Asignar repartidor - error si estado no es PENDIENTE")
    void asignarRepartidor_ErrorEstado() {
        // Given
        String pedidoId = "ped-123";
        pedido.setEstado(EstadoPedido.ASIGNADO);
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

        // When & Then
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> pedidoService.asignarRepartidorYVehiculo(pedidoId, "r","v"));
        assertEquals("Solo se pueden asignar recursos a pedidos en estado PENDIENTE", ex.getMessage());
        verify(pedidoRepository).findById(pedidoId);
    }

    @Test
    @DisplayName("Obtener pedidos pendientes de asignación exitosamente")
    void getPedidosPendientesAsignacion_Exitoso() {
        // Given
        List<Pedido> pendientes = List.of(pedido);
        when(pedidoRepository.findPedidosPendientesAsignacion()).thenReturn(pendientes);
        when(pedidoMapper.toResponse(pedido)).thenReturn(pedidoResponse);

        // When
        List<PedidoResponse> result = pedidoService.getPedidosPendientesAsignacion();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(pedidoRepository).findPedidosPendientesAsignacion();
        verify(pedidoMapper).toResponse(pedido);
    }

    @Test
    @DisplayName("Obtener pedidos por repartidor exitosamente")
    void getPedidosByRepartidor_Exitoso() {
        // Given
        String repartidorId = "rep-1";
        List<Pedido> pedidos = List.of(pedido);
        when(pedidoRepository.findByRepartidorId(repartidorId)).thenReturn(pedidos);
        when(pedidoMapper.toResponse(pedido)).thenReturn(pedidoResponse);

        // When
        List<PedidoResponse> result = pedidoService.getPedidosByRepartidor(repartidorId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(pedidoRepository).findByRepartidorId(repartidorId);
        verify(pedidoMapper).toResponse(pedido);
    }

    @Test
    @DisplayName("Obtener pedidos por modalidad exitosamente")
    void getPedidosByModalidad_Exitoso() {
        // Given
        ModalidadServicio modalidad = ModalidadServicio.NACIONAL;
        List<Pedido> pedidos = List.of(pedido);
        when(pedidoRepository.findByModalidadServicio(modalidad)).thenReturn(pedidos);
        when(pedidoMapper.toResponse(pedido)).thenReturn(pedidoResponse);

        // When
        List<PedidoResponse> result = pedidoService.getPedidosByModalidad(modalidad);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(pedidoRepository).findByModalidadServicio(modalidad);
        verify(pedidoMapper).toResponse(pedido);
    }

    @Test
    @DisplayName("Asociar factura exitosamente")
    void asociarFactura_Exitoso() {
        // Given
        String pedidoId = "ped-123";
        String facturaId = "fac-1";
        Double tarifa = 10.0;
        pedido.setFacturaId(null);

        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        when(pedidoMapper.toResponse(any(Pedido.class))).thenReturn(pedidoResponse);

        // When
        PedidoResponse result = pedidoService.asociarFactura(pedidoId, facturaId, tarifa);

        // Then
        assertNotNull(result);
        verify(pedidoRepository).findById(pedidoId);
        verify(pedidoRepository).save(any(Pedido.class));
        verify(pedidoMapper).toResponse(any(Pedido.class));
    }

    @Test
    @DisplayName("Asociar factura - error si ya tiene factura")
    void asociarFactura_ErrorYaTieneFactura() {
        // Given
        String pedidoId = "ped-123";
        pedido.setFacturaId("fac-existente");
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

        // When & Then
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> pedidoService.asociarFactura(pedidoId, "fac-2", 5.0));
        assertEquals("El pedido ya tiene una factura asociada", ex.getMessage());
        verify(pedidoRepository).findById(pedidoId);
    }

}
