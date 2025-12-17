package com.logiflow.pedidoservice.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests para la entidad Pedido")
class PedidoTest {

    private Pedido pedido;
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

        pedido = Pedido.builder()
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
    @DisplayName("Debería crear un pedido con todos los campos requeridos")
    void deberiaCrearPedidoConCamposRequeridos() {
        assertNotNull(pedido);
        assertEquals("cli-12345", pedido.getClienteId());
        assertEquals(direccionOrigen, pedido.getDireccionOrigen());
        assertEquals(direccionDestino, pedido.getDireccionDestino());
        assertEquals(ModalidadServicio.NACIONAL, pedido.getModalidadServicio());
        assertEquals(TipoEntrega.EXPRESS, pedido.getTipoEntrega());
        assertEquals(2.5, pedido.getPeso());
        assertEquals("0987654321", pedido.getTelefonoContacto());
        assertEquals("Juan Pérez", pedido.getNombreDestinatario());
    }

    @Test
    @DisplayName("Debería establecer estado PENDIENTE por defecto en onCreate")
    void deberiaEstablecerEstadoPendientePorDefecto() {
        // Simular el método @PrePersist
        pedido.onCreate();

        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
        assertNotNull(pedido.getFechaCreacion());
        assertNotNull(pedido.getFechaActualizacion());
    }

    @Test
    @DisplayName("Debería establecer prioridad ALTA para entrega EXPRESS en onCreate")
    void deberiaEstablecerPrioridadAltaParaExpress() {
        pedido.setTipoEntrega(TipoEntrega.EXPRESS);
        pedido.onCreate();

        assertEquals(Prioridad.ALTA, pedido.getPrioridad());
    }

    @Test
    @DisplayName("Debería establecer prioridad NORMAL para entrega NORMAL en onCreate")
    void deberiaEstablecerPrioridadNormalParaNormal() {
        pedido.setTipoEntrega(TipoEntrega.NORMAL);
        pedido.onCreate();

        assertEquals(Prioridad.NORMAL, pedido.getPrioridad());
    }

    @Test
    @DisplayName("Debería establecer prioridad NORMAL para entrega PROGRAMADA en onCreate")
    void deberiaEstablecerPrioridadNormalParaProgramada() {
        pedido.setTipoEntrega(TipoEntrega.PROGRAMADA);
        pedido.onCreate();

        assertEquals(Prioridad.NORMAL, pedido.getPrioridad());
    }

    @Test
    @DisplayName("Debería actualizar fechaActualizacion en onUpdate")
    void deberiaActualizarFechaActualizacionEnOnUpdate() {
        pedido.onCreate();
        LocalDateTime fechaCreacionOriginal = pedido.getFechaCreacion();
        LocalDateTime fechaActualizacionOriginal = pedido.getFechaActualizacion();

        // Simular paso del tiempo
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        pedido.onUpdate();

        assertEquals(fechaCreacionOriginal, pedido.getFechaCreacion()); // No debe cambiar
        assertTrue(pedido.getFechaActualizacion().isAfter(fechaActualizacionOriginal));
    }

    @Test
    @DisplayName("Debería establecer fechaEntregaReal cuando estado cambia a ENTREGADO en onUpdate")
    void deberiaEstablecerFechaEntregaRealCuandoEntregado() {
        pedido.onCreate();
        pedido.setEstado(EstadoPedido.ENTREGADO);
        pedido.onUpdate();

        assertNotNull(pedido.getFechaEntregaReal());
    }

    @Test
    @DisplayName("No debería cambiar fechaEntregaReal si ya está establecida")
    void noDeberiaCambiarFechaEntregaRealSiYaEstaEstablecida() {
        pedido.onCreate();
        pedido.setEstado(EstadoPedido.ENTREGADO);
        LocalDateTime fechaEntregaEsperada = LocalDateTime.now().minusHours(1);
        pedido.setFechaEntregaReal(fechaEntregaEsperada);

        pedido.onUpdate();

        assertEquals(fechaEntregaEsperada, pedido.getFechaEntregaReal());
    }

    @Test
    @DisplayName("Debería permitir construir pedido con builder pattern")
    void deberiaPermitirBuilderPattern() {
        Pedido pedidoBuilder = Pedido.builder()
                .clienteId("cli-test")
                .direccionOrigen(direccionOrigen)
                .direccionDestino(direccionDestino)
                .modalidadServicio(ModalidadServicio.URBANA_RAPIDA)
                .tipoEntrega(TipoEntrega.NORMAL)
                .peso(1.0)
                .telefonoContacto("0123456789")
                .build();

        assertNotNull(pedidoBuilder);
        assertEquals("cli-test", pedidoBuilder.getClienteId());
        assertEquals(ModalidadServicio.URBANA_RAPIDA, pedidoBuilder.getModalidadServicio());
        assertEquals(TipoEntrega.NORMAL, pedidoBuilder.getTipoEntrega());
        assertEquals(1.0, pedidoBuilder.getPeso());
    }

    @Test
    @DisplayName("Debería soportar todos los estados de pedido")
    void deberiaSoportarTodosLosEstados() {
        for (EstadoPedido estado : EstadoPedido.values()) {
            pedido.setEstado(estado);
            assertEquals(estado, pedido.getEstado());
        }
    }

    @Test
    @DisplayName("Debería soportar todas las modalidades de servicio")
    void deberiaSoportarTodasLasModalidades() {
        for (ModalidadServicio modalidad : ModalidadServicio.values()) {
            pedido.setModalidadServicio(modalidad);
            assertEquals(modalidad, pedido.getModalidadServicio());
        }
    }

    @Test
    @DisplayName("Debería soportar todos los tipos de entrega")
    void deberiaSoportarTodosLosTiposEntrega() {
        for (TipoEntrega tipo : TipoEntrega.values()) {
            pedido.setTipoEntrega(tipo);
            assertEquals(tipo, pedido.getTipoEntrega());
        }
    }
}
