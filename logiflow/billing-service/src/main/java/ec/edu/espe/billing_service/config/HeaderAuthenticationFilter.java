package ec.edu.espe.billing_service.config;

import ec.edu.espe.billing_service.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(HeaderAuthenticationFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            try {
                if (jwtUtils.validateToken(token)) {
                    String username = jwtUtils.extractUsername(token);
                    List<String> roles = jwtUtils.extractRoles(token);
                    
                    log.info(" BILLING-SERVICE - Usuario: {}, Roles: {}", username, roles);
                    
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .toList();

                    UsernamePasswordAuthenticationToken auth = 
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    
                    log.info(" AUTENTICACIÓN ESTABLECIDA - Usuario: {}, Authorities: {}", username, authorities);
                } else {
                    log.error(" BILLING-SERVICE - Token JWT inválido");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\":\"Token JWT inválido\"}");
                    return;
                }
            } catch (Exception e) {
                log.error(" BILLING-SERVICE - Error validando JWT: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Error de autenticación\"}");
                return;
            }
        } else {
            log.warn(" BILLING-SERVICE - Sin token Authorization");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Token requerido\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}

