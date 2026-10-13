package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hashes passwords with SHA-256 so plain text passwords are never stored.
 * The output is 64 lowercase hex characters - the same as MySQL's SHA2(x, 256),
 * which is why the sample data in the SQL script can log in.
 */
public class PasswordUtil {

    public static String hash(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));   // each byte -> 2 hex digits
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
