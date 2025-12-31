package logiflow.authservice.utils;

import logiflow.authservice.model.Role;
import logiflow.authservice.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    @Value("${jwt.secret:VGhpcy1pcy1hLXNlY3JldC1rZXktZm9y-LWRlbW8tYXBwLWp3dC0yMDI1}")
    private String secret;

    @Value("${jwt.expiration.ms:3600000}")
    private long jwtExpirationMs;

    @Value("${jwt.refresh.expiration.ms:1209600000}") // 14 days
    private long refreshExpirationMs;

    @Value("${jwt.issuer:auth-service}")
    private String issuer;

    private byte[] secretKeyBytes() {
        // Try to base64-decode; if fails, use raw bytes
        try {
            return Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException e) {
            return secret.getBytes(StandardCharsets.UTF_8);
        }
    }

    public String generateAccessToken(User user) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", user.getUsername());
        payload.put("iss", issuer);
        long now = System.currentTimeMillis();
        payload.put("iat", now / 1000);
        payload.put("exp", (now + jwtExpirationMs) / 1000);
        payload.put("roles", user.getRoles().stream().map(Role::getName).map(Enum::name).collect(Collectors.toList()));
        return buildToken(payload);
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", user.getUsername());
        payload.put("iss", issuer);
        long now = System.currentTimeMillis();
        payload.put("iat", now / 1000);
        payload.put("exp", (now + refreshExpirationMs) / 1000);
        return buildToken(payload);
    }

    public boolean validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;
            String header = parts[0];
            String payload = parts[1];
            String signature = parts[2];

            String expectedSig = sign(header + "." + payload);
            if (!constantTimeEquals(signature, expectedSig)) return false;

            String payloadJson = new String(base64UrlDecode(payload), StandardCharsets.UTF_8);
            Long exp = extractLongClaim(payloadJson, "exp");
            if (exp == null) return false;
            long nowSec = System.currentTimeMillis() / 1000;
            return nowSec < exp;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;
            String payload = parts[1];
            String payloadJson = new String(base64UrlDecode(payload), StandardCharsets.UTF_8);
            return extractStringClaim(payloadJson, "sub");
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> extractRoles(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return Collections.emptyList();
            String payload = parts[1];
            String payloadJson = new String(base64UrlDecode(payload), StandardCharsets.UTF_8);
            return extractStringListClaim(payloadJson, "roles");
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // Helpers
    private String buildToken(Map<String, Object> payload) {
        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payloadJson = toJson(payload);
        String header = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
        String body = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signature = sign(header + "." + body);
        return header + "." + body + "." + signature;
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKeyBytes(), "HmacSHA256"));
            byte[] sig = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(sig);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign JWT", e);
        }
    }

    private static String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
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

    private static String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(escape(e.getKey())).append('"').append(':');
            Object v = e.getValue();
            if (v instanceof String) {
                sb.append('"').append(escape((String) v)).append('"');
            } else if (v instanceof Number || v instanceof Boolean) {
                sb.append(v.toString());
            } else if (v instanceof Collection) {
                sb.append('[');
                boolean f2 = true;
                for (Object item : (Collection<?>) v) {
                    if (!f2) sb.append(',');
                    f2 = false;
                    sb.append('"').append(escape(String.valueOf(item))).append('"');
                }
                sb.append(']');
            } else {
                sb.append('"').append(escape(String.valueOf(v))).append('"');
            }
        }
        sb.append('}');
        return sb.toString();
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static Long extractLongClaim(String json, String key) {
        // naive parser: find "key":number
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
        // naive parser: find "key":"value"
        String pattern = '"' + key + '"' + ":\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return null;
        int start = idx + pattern.length();
        int end = json.indexOf('"', start);
        if (end < 0) return null;
        return json.substring(start, end);
    }

    private static List<String> extractStringListClaim(String json, String key) {
        // naive parser for array of strings: "key":["A","B"]
        String pattern = '"' + key + '"' + ":[";
        int idx = json.indexOf(pattern);
        if (idx < 0) return Collections.emptyList();
        int start = idx + pattern.length();
        int end = json.indexOf(']', start);
        if (end < 0) return Collections.emptyList();
        String inside = json.substring(start, end);
        if (inside.trim().isEmpty()) return Collections.emptyList();
        String[] items = inside.split(",");
        List<String> result = new ArrayList<>();
        for (String it : items) {
            String s = it.trim();
            if (s.startsWith("\"") && s.endsWith("\"")) {
                s = s.substring(1, s.length() - 1);
            }
            result.add(s);
        }
        return result;
    }
}
