package com.logiflow.pedidoservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Filtro JWT simplificado para autenticación en pedido-service
 * Compatible con tokens generados por auth-service
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.issuer}")
  private String jwtIssuer;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  protected void doFilterInternal(
          HttpServletRequest request,
          HttpServletResponse response,
          FilterChain filterChain
  ) throws ServletException, IOException {

    try {
      String token = extractToken(request);

      if (token != null && validateToken(token)) {
        Map<String, Object> claims = parseToken(token);

        // Validar issuer
        String issuer = (String) claims.get("iss");
        if (!jwtIssuer.equals(issuer)) {
          log.warn("JWT issuer inválido: {}", issuer);
          filterChain.doFilter(request, response);
          return;
        }

        // Extraer roles
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.getOrDefault("roles", List.of());

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());

        String username = (String) claims.get("sub");

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
      String[] parts = token.split("\\.");
      if (parts.length != 3) return false;

      String header = parts[0];
      String payload = parts[1];
      String signature = parts[2];

      // Verificar firma
      String expectedSignature = sign(header + "." + payload);
      if (!constantTimeEquals(signature, expectedSignature)) {
        return false;
      }

      // Verificar expiración
      Map<String, Object> claims = decodePayload(payload);
      Number exp = (Number) claims.get("exp");
      if (exp == null) return false;

      long nowSec = System.currentTimeMillis() / 1000;
      return nowSec < exp.longValue();
    } catch (Exception e) {
      log.error("Error validando token: {}", e.getMessage());
      return false;
    }
  }

  private Map<String, Object> parseToken(String token) {
    String[] parts = token.split("\\.");
    if (parts.length != 3) {
      throw new IllegalArgumentException("Token JWT inválido");
    }
    return decodePayload(parts[1]);
  }

  private Map<String, Object> decodePayload(String payload) {
    try {
      byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
      String json = new String(decodedBytes, StandardCharsets.UTF_8);
      @SuppressWarnings("unchecked")
      Map<String, Object> claims = objectMapper.readValue(json, Map.class);
      return claims;
    } catch (Exception e) {
      throw new RuntimeException("Error decodificando payload JWT", e);
    }
  }

  private String sign(String data) {
    try {
      // Manejar secret Base64 como auth-service
      byte[] keyBytes;
      try {
        keyBytes = Base64.getDecoder().decode(jwtSecret);
      } catch (IllegalArgumentException e) {
        keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
      }

      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(keyBytes, "HmacSHA256"));
      byte[] sig = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(sig);
    } catch (Exception e) {
      throw new RuntimeException("Error firmando JWT", e);
    }
  }

  private boolean constantTimeEquals(String a, String b) {
    if (a == null || b == null) return false;
    if (a.length() != b.length()) return false;
    int result = 0;
    for (int i = 0; i < a.length(); i++) {
      result |= a.charAt(i) ^ b.charAt(i);
    }
    return result == 0;
  }
}
