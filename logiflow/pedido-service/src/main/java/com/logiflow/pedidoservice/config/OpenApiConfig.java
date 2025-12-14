package com.logiflow.pedidoservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI 3.0 para documentación automática
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pedidoServiceOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8084");
        localServer.setDescription("Servidor local de desarrollo");

        Contact contact = new Contact();
        contact.setName("LogiFlow Team");
        contact.setEmail("support@logiflow.com");

        License license = new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0.html");

        Info info = new Info()
                .title("Pedido Service API")
                .version("1.0.0")
                .description("API REST para la gestión de pedidos de entrega. " +
                        "Incluye operaciones CRUD completas con validación de cobertura geográfica " +
                        "y tipo de entrega. Todas las operaciones de escritura son transaccionales (ACID).")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer));
    }
}
