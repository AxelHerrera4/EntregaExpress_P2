package ec.edu.espe.api_gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component  // SIMPLIFICADO - solo hace routing
public class JwtGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            log.info(" TOKEN DETECTADO en {} - Reenviando a microservicio para validación", path);
        } else {
            log.info(" SIN TOKEN en {} - Microservicio manejará la autenticación", path);
        }
        
        // Simplemente pasar la petición al microservicio
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}