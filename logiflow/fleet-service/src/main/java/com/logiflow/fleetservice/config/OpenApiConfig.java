package com.logiflow.fleetservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de documentación OpenAPI 3.0
 * Accesible en /swagger-ui.html
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "FleetService API",
                version = "1.0.0",
                description = "API REST para gestión de flota de vehículos y repartidores - LogiFlow",
                contact = @Contact(
                        name = "EntregaExpress S.A.",
                        email = "soporte@entregaexpress.com"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8083/api",
                        description = "Servidor de Desarrollo"
                )
        }
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "Token JWT obtenido del AuthService. Formato: Bearer {token}"
)
public class OpenApiConfig {
}