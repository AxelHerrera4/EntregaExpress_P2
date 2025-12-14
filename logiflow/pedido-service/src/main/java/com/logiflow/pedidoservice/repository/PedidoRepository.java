package com.logiflow.pedidoservice.repository;

import com.logiflow.pedidoservice.model.EstadoPedido;
import com.logiflow.pedidoservice.model.ModalidadServicio;
import com.logiflow.pedidoservice.model.Pedido;
import com.logiflow.pedidoservice.model.Prioridad;
import com.logiflow.pedidoservice.model.TipoEntrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, String> {

    // Búsquedas básicas
    List<Pedido> findByClienteId(String clienteId);

    List<Pedido> findByEstado(EstadoPedido estado);

    List<Pedido> findByTipoEntrega(TipoEntrega tipoEntrega);

    List<Pedido> findByCobertura(String cobertura);

    // Búsquedas por modalidad de servicio
    List<Pedido> findByModalidadServicio(ModalidadServicio modalidadServicio);

    // Búsquedas para integración con FleetService
    List<Pedido> findByRepartidorId(String repartidorId);

    List<Pedido> findByVehiculoId(String vehiculoId);

    // Pedidos pendientes de asignación (sin repartidor ni vehículo)
    @Query("SELECT p FROM Pedido p WHERE p.repartidorId IS NULL AND p.vehiculoId IS NULL AND p.estado = 'PENDIENTE'")
    List<Pedido> findPedidosPendientesAsignacion();

    // Pedidos en tránsito por repartidor
    List<Pedido> findByRepartidorIdAndEstado(String repartidorId, EstadoPedido estado);

    // Búsquedas para integración con BillingService
    List<Pedido> findByFacturaId(String facturaId);

    // Pedidos sin factura generada
    @Query("SELECT p FROM Pedido p WHERE p.facturaId IS NULL")
    List<Pedido> findPedidosSinFactura();

    // Pedidos por prioridad
    List<Pedido> findByPrioridad(Prioridad prioridad);

    // Pedidos de alta prioridad pendientes
    @Query("SELECT p FROM Pedido p WHERE p.prioridad IN ('ALTA', 'URGENTE') AND p.estado IN ('PENDIENTE', 'ASIGNADO')")
    List<Pedido> findPedidosAltaPrioridadPendientes();
}


