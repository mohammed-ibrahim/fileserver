package org.example.fileserver;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TokenUtils {

    public static String generateToken(String username, String password) {
        return base64Encode(username + ":" + password);
    }

    public static String[] decodeToken(String token) {
        String[] parts = base64Decode(token).split(":", 2);
        return parts;
    }

    public static String base64Encode(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String base64Decode(String input) {
        return new String(Base64.getDecoder().decode(input), StandardCharsets.UTF_8);
    }
}
