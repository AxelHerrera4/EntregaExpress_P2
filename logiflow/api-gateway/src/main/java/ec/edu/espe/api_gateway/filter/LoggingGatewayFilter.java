package ec.edu.espe.api_gateway.filter;


import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class LoggingGatewayFilter extends AbstractGatewayFilterFactory<LoggingGatewayFilter.Config> {

    public LoggingGatewayFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            HttpHeaders headers = exchange.getRequest().getHeaders();

            System.out.println("\n=== GATEWAY RECIBE PETICIÓN ===");
            System.out.println("PATH: " + exchange.getRequest().getURI());
            System.out.println("Authorization: " + headers.getFirst("Authorization"));
            System.out.println("X-User: " + headers.getFirst("X-User"));
            System.out.println("X-Roles: " + headers.getFirst("X-Roles"));

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                System.out.println("=== GATEWAY ENVÍA HACIA BILLING ===");
                System.out.println("Authorization: " +
                        exchange.getRequest().getHeaders().getFirst("Authorization"));
                System.out.println("X-User: " +
                        exchange.getRequest().getHeaders().getFirst("X-User"));
                System.out.println("X-Roles: " +
                        exchange.getRequest().getHeaders().getFirst("X-Roles"));
                System.out.println("====================================\n");
            }));
        };
    }

    public static class Config {
        // vacío pero necesario
    }
}

