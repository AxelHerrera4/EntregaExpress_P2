package ec.edu.espe.api_gateway.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Component
public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String secret;

    private byte[] secretKeyBytes() {
        try {
            return Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException e) {
            return secret.getBytes(StandardCharsets.UTF_8);
        }
    }

    public boolean validateToken(String token) {
        try {
            log.info("=== VALIDANDO JWT EN GATEWAY ===");
            log.info("Token recibido: {}", token);
            log.info(" Clave usada en Gateway: {}", secret);

            // Usar la misma lógica que auth-service
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                log.error(" Token no tiene 3 partes");
                return false;
            }
            
            String header = parts[0];
            String payload = parts[1];
            String signature = parts[2];

            String expectedSig = sign(header + "." + payload);
            if (!constantTimeEquals(signature, expectedSig)) {
                log.error(" Firma no coincide");
                return false;
            }

            String payloadJson = new String(base64UrlDecode(payload), StandardCharsets.UTF_8);
            Long exp = extractLongClaim(payloadJson, "exp");
            if (exp == null) {
                log.error(" No hay claim 'exp'");
                return false;
            }
            
            long nowSec = System.currentTimeMillis() / 1000;
            if (nowSec >= exp) {
                log.error(" Token expirado");
                return false;
            }

            log.info(" JWT VÁLIDO");
            return true;

        } catch (Exception e) {
            log.error(" ERROR VALIDANDO JWT: {}", e.getMessage());
            return false;
        }
    }

    public String extractUsername(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;
            String payloadJson = new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
            return extractStringClaim(payloadJson, "sub");
        } catch (Exception e) {
            log.error("Error extrayendo username: {}", e.getMessage());
            return null;
        }
    }

    public List<String> extractRoles(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return List.of();
            String payloadJson = new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
            List<String> roles = extractStringListClaim(payloadJson, "roles");
            log.info("RAW roles desde JWT: {}", roles);
            log.info("Tipo de rolesObj: {}", roles.getClass());
            return roles;
        } catch (Exception e) {
            log.error("Error extrayendo roles: {}", e.getMessage());
            return List.of();
        }
    }

    // === MÉTODOS AUXILIARES COPIADOS DEL AUTH-SERVICE ===
    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKeyBytes(), "HmacSHA256"));
            byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception e) {
            throw new RuntimeException("Error signing JWT", e);
        }
    }

    private static byte[] base64UrlDecode(String str) {
        return Base64.getUrlDecoder().decode(str);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    private static Long extractLongClaim(String json, String key) {
        String pattern = '"' + key + '"' + ":";
        int idx = json.indexOf(pattern);
        if (idx < 0) return null;
        int start = idx + pattern.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)))) {
            end++;
        }
        try {
            return Long.parseLong(json.substring(start, end));
        } catch (Exception e) {
            return null;
        }
    }

    private static String extractStringClaim(String json, String key) {
        String pattern = '"' + key + '"' + ":\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return null;
        int start = idx + pattern.length();
        int end = json.indexOf('"', start);
        if (end < 0) return null;
        return json.substring(start, end);
    }

    private static List<String> extractStringListClaim(String json, String key) {
        String pattern = '"' + key + '"' + ":\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return Collections.emptyList();
        int start = idx + pattern.length();
        int end = json.indexOf('"', start);
        if (end < 0) return Collections.emptyList();
        String rolesStr = json.substring(start, end);
        if (rolesStr.trim().isEmpty()) return Collections.emptyList();
        return Arrays.asList(rolesStr.split(","));
    }
}
