package com.logiflow.pedidoservice.repository;

import com.logiflow.pedidoservice.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Tests para PedidoRepository - Tests Unitarios Estáticos con JUnit")
class PedidoRepositoryTest {


    @Autowired
    private PedidoRepository pedidoRepository;

    private Pedido pedidoSample;
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

        pedidoSample = Pedido.builder()
                .clienteId("cli-12345")
                .direccionOrigen(direccionOrigen)
                .direccionDestino(direccionDestino)
                .modalidadServicio(ModalidadServicio.NACIONAL)
                .tipoEntrega(TipoEntrega.EXPRESS)
                .estado(EstadoPedido.PENDIENTE)
                .peso(2.5)
                .cobertura("NACIONAL")
                .telefonoContacto("0987654321")
                .nombreDestinatario("Juan Pérez")
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .prioridad(Prioridad.ALTA)
                .build();
    }

    @Test
    @DisplayName("Debería guardar y encontrar un pedido por ID")
    void deberiaGuardarYEncontrarPedidoPorId() {
        // Given
        Pedido pedidoGuardado = pedidoRepository.save(pedidoSample);
        assertNotNull(pedidoGuardado.getId());

        // When
        Optional<Pedido> pedidoEncontrado = pedidoRepository.findById(pedidoGuardado.getId());

        // Then
        assertTrue(pedidoEncontrado.isPresent());
        assertEquals(pedidoSample.getClienteId(), pedidoEncontrado.get().getClienteId());
        assertEquals(pedidoSample.getDireccionOrigen(), pedidoEncontrado.get().getDireccionOrigen());
        assertEquals(pedidoSample.getDireccionDestino(), pedidoEncontrado.get().getDireccionDestino());
        assertEquals(pedidoSample.getModalidadServicio(), pedidoEncontrado.get().getModalidadServicio());
        assertEquals(pedidoSample.getTipoEntrega(), pedidoEncontrado.get().getTipoEntrega());
        assertEquals(pedidoSample.getEstado(), pedidoEncontrado.get().getEstado());
    }

    @Test
    @DisplayName("Debería encontrar pedidos por clienteId")
    void deberiaEncontrarPedidosPorClienteId() {
        // Given
        String clienteId = "cli-test-123";

        Pedido pedido1 = crearPedidoConCliente(clienteId, TipoEntrega.EXPRESS);
        Pedido pedido2 = crearPedidoConCliente(clienteId, TipoEntrega.NORMAL);
        Pedido pedido3 = crearPedidoConCliente("otro-cliente", TipoEntrega.EXPRESS);

        pedidoRepository.save(pedido1);
        pedidoRepository.save(pedido2);
        pedidoRepository.save(pedido3);

        // When
        List<Pedido> pedidosCliente = pedidoRepository.findByClienteId(clienteId);

        // Then
        assertEquals(2, pedidosCliente.size());
        assertTrue(pedidosCliente.stream().allMatch(p -> p.getClienteId().equals(clienteId)));
    }

    @Test
    @DisplayName("Debería encontrar pedidos por estado")
    void deberiaEncontrarPedidosPorEstado() {
        // Given
        Pedido pedidoPendiente = crearPedidoConEstado(EstadoPedido.PENDIENTE);
        Pedido pedidoEnTransito = crearPedidoConEstado(EstadoPedido.EN_TRANSITO);
        Pedido pedidoEntregado = crearPedidoConEstado(EstadoPedido.ENTREGADO);

        pedidoRepository.save(pedidoPendiente);
        pedidoRepository.save(pedidoEnTransito);
        pedidoRepository.save(pedidoEntregado);

        // When
        List<Pedido> pedidosPendientes = pedidoRepository.findByEstado(EstadoPedido.PENDIENTE);

        // Then
        assertEquals(1, pedidosPendientes.size());
        assertEquals(EstadoPedido.PENDIENTE, pedidosPendientes.getFirst().getEstado());
    }

    @Test
    @DisplayName("Debería encontrar pedidos por repartidorId")
    void deberiaEncontrarPedidosPorRepartidorId() {
        // Given
        String repartidorId = "rep-001";

        Pedido pedido1 = crearPedidoConRepartidor(repartidorId);
        Pedido pedido2 = crearPedidoConRepartidor(repartidorId);
        Pedido pedido3 = crearPedidoConRepartidor("rep-002");

        pedidoRepository.save(pedido1);
        pedidoRepository.save(pedido2);
        pedidoRepository.save(pedido3);

        // When
        List<Pedido> pedidosRepartidor = pedidoRepository.findByRepartidorId(repartidorId);

        // Then
        assertEquals(2, pedidosRepartidor.size());
        assertTrue(pedidosRepartidor.stream().allMatch(p -> p.getRepartidorId().equals(repartidorId)));
    }

    @Test
    @DisplayName("Debería contar pedidos por estado")
    void deberiaContarPedidosPorEstado() {
        // Given
        pedidoRepository.save(crearPedidoConEstado(EstadoPedido.PENDIENTE));
        pedidoRepository.save(crearPedidoConEstado(EstadoPedido.PENDIENTE));
        pedidoRepository.save(crearPedidoConEstado(EstadoPedido.EN_TRANSITO));

        // When
        long countPendientes = pedidoRepository.countByEstado(EstadoPedido.PENDIENTE);
        long countEnTransito = pedidoRepository.countByEstado(EstadoPedido.EN_TRANSITO);
        long countEntregados = pedidoRepository.countByEstado(EstadoPedido.ENTREGADO);

        // Then
        assertEquals(2, countPendientes);
        assertEquals(1, countEnTransito);
        assertEquals(0, countEntregados);
    }

    @Test
    @DisplayName("Debería encontrar pedidos por modalidad de servicio")
    void deberiaEncontrarPedidosPorModalidadServicio() {
        // Given
        Pedido pedidoUrbano = crearPedidoConModalidad(ModalidadServicio.URBANA_RAPIDA);
        Pedido pedidoIntermunicipal = crearPedidoConModalidad(ModalidadServicio.INTERMUNICIPAL);
        Pedido pedidoNacional = crearPedidoConModalidad(ModalidadServicio.NACIONAL);

        pedidoRepository.save(pedidoUrbano);
        pedidoRepository.save(pedidoIntermunicipal);
        pedidoRepository.save(pedidoNacional);

        // When
        List<Pedido> pedidosUrbanos = pedidoRepository.findByModalidadServicio(ModalidadServicio.URBANA_RAPIDA);

        // Then
        assertEquals(1, pedidosUrbanos.size());
        assertEquals(ModalidadServicio.URBANA_RAPIDA, pedidosUrbanos.getFirst().getModalidadServicio());
    }

    @Test
    @DisplayName("Debería actualizar un pedido existente")
    void deberiaActualizarPedidoExistente() {
        // Given
        Pedido pedidoGuardado = pedidoRepository.save(pedidoSample);
        String idPedido = pedidoGuardado.getId();

        // When
        pedidoGuardado.setEstado(EstadoPedido.EN_TRANSITO);
        pedidoGuardado.setRepartidorId("rep-001");
        pedidoRepository.save(pedidoGuardado);

        // Then
        Optional<Pedido> pedidoEncontrado = pedidoRepository.findById(idPedido);
        assertTrue(pedidoEncontrado.isPresent());
        assertEquals(EstadoPedido.EN_TRANSITO, pedidoEncontrado.get().getEstado());
        assertEquals("rep-001", pedidoEncontrado.get().getRepartidorId());
    }

    @Test
    @DisplayName("Debería eliminar un pedido")
    void deberiaEliminarPedido() {
        // Given
        Pedido pedidoGuardado = pedidoRepository.save(pedidoSample);
        String idPedido = pedidoGuardado.getId();

        // When
        pedidoRepository.deleteById(idPedido);

        // Then
        Optional<Pedido> pedidoEncontrado = pedidoRepository.findById(idPedido);
        assertFalse(pedidoEncontrado.isPresent());
    }

    @Test
    @DisplayName("Debería manejar direcciones embebidas correctamente")
    void deberiaManejarDireccionesEmebidasCorrectamente() {
        // Given
        Pedido pedidoGuardado = pedidoRepository.save(pedidoSample);

        // When
        Optional<Pedido> pedidoEncontrado = pedidoRepository.findById(pedidoGuardado.getId());

        // Then
        assertTrue(pedidoEncontrado.isPresent());
        assertNotNull(pedidoEncontrado.get().getDireccionOrigen());
        assertNotNull(pedidoEncontrado.get().getDireccionDestino());
        assertEquals("Av Principal", pedidoEncontrado.get().getDireccionOrigen().getCalle());
        assertEquals("Calle Secundaria", pedidoEncontrado.get().getDireccionDestino().getCalle());
    }

    // Métodos auxiliares para crear pedidos de prueba
    private Pedido crearPedidoConCliente(String clienteId, TipoEntrega tipoEntrega) {
        return Pedido.builder()
                .clienteId(clienteId)
                .direccionOrigen(direccionOrigen)
                .direccionDestino(direccionDestino)
                .modalidadServicio(ModalidadServicio.NACIONAL)
                .tipoEntrega(tipoEntrega)
                .estado(EstadoPedido.PENDIENTE)
                .peso(1.0)
                .cobertura("NACIONAL")
                .telefonoContacto("0123456789")
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .prioridad(Prioridad.NORMAL)
                .build();
    }

    private Pedido crearPedidoConEstado(EstadoPedido estado) {
        return Pedido.builder()
                .clienteId("cli-test")
                .direccionOrigen(direccionOrigen)
                .direccionDestino(direccionDestino)
                .modalidadServicio(ModalidadServicio.NACIONAL)
                .tipoEntrega(TipoEntrega.NORMAL)
                .estado(estado)
                .peso(1.0)
                .cobertura("NACIONAL")
                .telefonoContacto("0123456789")
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .prioridad(Prioridad.NORMAL)
                .build();
    }

    private Pedido crearPedidoConRepartidor(String repartidorId) {
        return Pedido.builder()
                .clienteId("cli-test")
                .direccionOrigen(direccionOrigen)
                .direccionDestino(direccionDestino)
                .modalidadServicio(ModalidadServicio.NACIONAL)
                .tipoEntrega(TipoEntrega.NORMAL)
                .estado(EstadoPedido.ASIGNADO)
                .peso(1.0)
                .cobertura("NACIONAL")
                .telefonoContacto("0123456789")
                .repartidorId(repartidorId)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .prioridad(Prioridad.NORMAL)
                .build();
    }

    private Pedido crearPedidoConModalidad(ModalidadServicio modalidadServicio) {
        return Pedido.builder()
                .clienteId("cli-test")
                .direccionOrigen(direccionOrigen)
                .direccionDestino(direccionDestino)
                .modalidadServicio(modalidadServicio)
                .tipoEntrega(TipoEntrega.NORMAL)
                .estado(EstadoPedido.PENDIENTE)
                .peso(1.0)
                .cobertura("TEST")
                .telefonoContacto("0123456789")
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .prioridad(Prioridad.NORMAL)
                .build();
    }
}
