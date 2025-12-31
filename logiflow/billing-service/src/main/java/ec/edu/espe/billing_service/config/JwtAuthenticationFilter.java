package ec.edu.espe.billing_service.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filtro JWT para autenticación en billing-service
 * Sincronizado con auth-service, fleet-service y pedido-service
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.issuer}")
  private String jwtIssuer;

  @Override
  protected void doFilterInternal(
          HttpServletRequest request,
          HttpServletResponse response,
          FilterChain filterChain) throws ServletException, IOException {

    try {
      String token = extractToken(request);

      if (token != null && validateToken(token)) {
        Claims claims = parseToken(token);

        // Validar issuer
        if (!jwtIssuer.equals(claims.getIssuer())) {
          log.warn("Token JWT con issuer incorrecto: {}", claims.getIssuer());
          SecurityContextHolder.clearContext();
          filterChain.doFilter(request, response);
          return;
        }

        // Extraer roles y agregar prefijo ROLE_
        List<String> roles = claims.get("roles", List.class);

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());

        String username = claims.getSubject();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, authorities);

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("JWT validado para usuario: {} con roles: {}", username, roles);
      }
    } catch (Exception e) {
      log.error("Error en autenticación JWT: {}", e.getMessage());
      SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
  }

  private String extractToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");

    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }

    return null;
  }

  private boolean validateToken(String token) {
    try {
      parseToken(token);
      return true;
    } catch (Exception e) {
      log.error("Token JWT inválido: {}", e.getMessage());
      return false;
    }
  }

  private Claims parseToken(String token) {
    // Handle Base64 encoded secret like auth-service does
    byte[] keyBytes;
    try {
      keyBytes = java.util.Base64.getDecoder().decode(jwtSecret);
    } catch (IllegalArgumentException e) {
      keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
    }

    SecretKey key = Keys.hmacShaKeyFor(keyBytes);

    return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
  }
}
