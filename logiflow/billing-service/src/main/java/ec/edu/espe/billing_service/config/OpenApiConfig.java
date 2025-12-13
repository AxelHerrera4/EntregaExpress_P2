package ec.edu.espe.billing_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI billingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Billing Service API")
                        .description(
                                "Microservicio de facturación del sistema LogiFlow. " +
                                        "Gestiona tarifas base y generación de facturas " +
                                        "utilizando patrones Strategy y Factory Method."
                        )
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Carrera de Ingeniería de Software - ESPE")
                                .email("info@espe.edu.ec"))
                        .license(new License()
                                .name("Uso académico"))
                );
    }
}
