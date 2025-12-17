package com.logiflow.pedidoservice;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Test Suite Completo - Pedido Service")
@SelectPackages({
    "com.logiflow.pedidoservice.model",
    "com.logiflow.pedidoservice.dto",
    "com.logiflow.pedidoservice.repository",
    "com.logiflow.pedidoservice.service",
    "com.logiflow.pedidoservice.controller",
    "com.logiflow.pedidoservice.integration"
})
public class PedidoServiceTestSuite {
    // Esta clase actúa como suite de tests para ejecutar todas las pruebas del microservicio
    // Utiliza JUnit 5 Platform Suite Engine para organizar y ejecutar los tests por paquetes
}
