package com.logiflow.pedidoservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Tests de configuración de la aplicación")
class PedidoServiceApplicationContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Debería cargar el contexto de Spring correctamente")
    void deberiaCargarElContextoDeSpringCorrectamente() {
        assertNotNull(applicationContext);
        assertTrue(applicationContext.getBeanDefinitionCount() > 0);
    }

    @Test
    @DisplayName("Debería tener todos los beans principales configurados")
    void deberiaTenerTodosLosBeanesPrincipalesConfigurados() {
        // Verificar que existen los beans principales
        assertTrue(applicationContext.containsBean("pedidoController"));
        assertTrue(applicationContext.containsBean("pedidoServiceImpl"));
        assertTrue(applicationContext.containsBean("pedidoRepository"));
        assertTrue(applicationContext.containsBean("pedidoMapper"));
        assertTrue(applicationContext.containsBean("coberturaValidationServiceImpl"));
    }
}
