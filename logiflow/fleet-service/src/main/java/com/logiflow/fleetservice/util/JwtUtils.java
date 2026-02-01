package com.logiflow.fleetservice.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Value("${jwt.secret:VGhpcy1pcy1hLXNlY3JldC1rZXktZm9yLWRlbW8tYXBwLWp3dC0yMDI1}")
    private String secret;

    private byte[] secretKeyBytes() {
        try {
            return Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException e) {
            return secret.getBytes(StandardCharsets.UTF_8);
        }
    }

    private String sign(String data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secretKeyBytes(), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    public boolean validateToken(String token) {
        try {
            log.info("=== VALIDANDO JWT EN PEDIDO-SERVICE ===");
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                log.error(" Token malformado: partes = {}", parts.length);
                return false;
            }

            String header = parts[0];
            String payload = parts[1];
            String providedSignature = parts[2];

            String data = header + "." + payload;
            String calculatedSignature = sign(data);

            if (!calculatedSignature.equals(providedSignature)) {
                log.error(" Firma inválida");
                return false;
            }

            // Verificar expiración
            String payloadJson = new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
            JsonNode payloadNode = mapper.readTree(payloadJson);

            if (payloadNode.has("exp")) {
                long exp = payloadNode.get("exp").asLong();
                long now = System.currentTimeMillis() / 1000;
                if (now > exp) {
                    log.error(" Token expirado");
                    return false;
                }
            }

            log.info(" JWT VÁLIDO en pedido-service");
            return true;

        } catch (Exception e) {
            log.error(" Error validando token: {}", e.getMessage());
            return false;
        }
    }

    public String extractUsername(String token) {
        try {
            String[] parts = token.split("\\.");
            String payload = parts[1];
            String payloadJson = new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
            JsonNode payloadNode = mapper.readTree(payloadJson);

            if (payloadNode.has("sub")) {
                return payloadNode.get("sub").asText();
            }
            return null;
        } catch (Exception e) {
            log.error(" Error extrayendo username: {}", e.getMessage());
            return null;
        }
    }

    public List<String> extractRoles(String token) {
        try {
            String[] parts = token.split("\\.");
            String payload = parts[1];
            String payloadJson = new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
            JsonNode payloadNode = mapper.readTree(payloadJson);

            if (payloadNode.has("roles")) {
                JsonNode rolesNode = payloadNode.get("roles");
                if (rolesNode.isTextual()) {
                    return Arrays.asList(rolesNode.asText().split(","));
                } else if (rolesNode.isArray()) {
                    List<String> roles = new ArrayList<>();
                    rolesNode.forEach(role -> roles.add(role.asText()));
                    return roles;
                }
            }
            return new ArrayList<>();
        } catch (Exception e) {
            log.error(" Error extrayendo roles: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
