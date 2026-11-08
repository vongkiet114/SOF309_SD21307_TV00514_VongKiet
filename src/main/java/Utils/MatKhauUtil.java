package Utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;

/**
 * Băm mật khẩu mới bằng PBKDF2 và vẫn xác minh được SHA-256 cũ để chuyển đổi dần.
 */
public final class MatKhauUtil {
    private static final String LEGACY_PEPPER = "qlsv$";
    private static final String PBKDF2_PREFIX = "pbkdf2";
    private static final int PBKDF2_ITERATIONS = 120_000;
    private static final int MIN_ACCEPTED_ITERATIONS = 10_000;
    private static final int MAX_ACCEPTED_ITERATIONS = 1_000_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private MatKhauUtil() {
    }

    public static String bam(String matKhauTho) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] derived = derive(normalize(matKhauTho), salt, PBKDF2_ITERATIONS, KEY_BITS);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return PBKDF2_PREFIX + "$" + PBKDF2_ITERATIONS + "$"
                + encoder.encodeToString(salt) + "$" + encoder.encodeToString(derived);
    }

    public static boolean khop(String matKhauTho, String matKhauBamDaLuu) {
        if (matKhauBamDaLuu == null || matKhauBamDaLuu.isEmpty()) {
            return false;
        }
        if (matKhauBamDaLuu.startsWith(PBKDF2_PREFIX + "$")) {
            return khopPbkdf2(normalize(matKhauTho), matKhauBamDaLuu);
        }
        return constantTimeTextEquals(bamLegacy(normalize(matKhauTho)), matKhauBamDaLuu);
    }

    public static boolean canNangCap(String matKhauBamDaLuu) {
        return matKhauBamDaLuu != null
                && !matKhauBamDaLuu.startsWith(PBKDF2_PREFIX + "$");
    }

    private static boolean khopPbkdf2(String matKhauTho, String storedHash) {
        try {
            String[] parts = storedHash.split("\\$", -1);
            if (parts.length != 4 || !PBKDF2_PREFIX.equals(parts[0])) {
                return false;
            }

            int iterations = Integer.parseInt(parts[1]);
            if (iterations < MIN_ACCEPTED_ITERATIONS || iterations > MAX_ACCEPTED_ITERATIONS) {
                return false;
            }

            Base64.Decoder decoder = Base64.getUrlDecoder();
            byte[] salt = decoder.decode(parts[2]);
            byte[] expected = decoder.decode(parts[3]);
            if (salt.length == 0 || expected.length == 0) {
                return false;
            }

            byte[] actual = derive(matKhauTho, salt, iterations, expected.length * 8);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derive(String matKhauTho, byte[] salt, int iterations, int keyBits) {
        PBEKeySpec spec = new PBEKeySpec(matKhauTho.toCharArray(), salt, iterations, keyBits);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Không thể tạo hash PBKDF2", e);
        } finally {
            spec.clearPassword();
        }
    }

    private static String bamLegacy(String matKhauTho) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(
                    (LEGACY_PEPPER + matKhauTho).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hashed.length * 2);
            for (byte value : hashed) {
                hex.append(String.format("%02X", value));
            }
            return hex.toString();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Không thể tạo hash SHA-256 cũ", e);
        }
    }

    private static boolean constantTimeTextEquals(String expected, String actual) {
        byte[] expectedBytes = expected.toUpperCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = actual.toUpperCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expectedBytes, actualBytes);
    }

    private static String normalize(String matKhauTho) {
        return matKhauTho == null ? "" : matKhauTho;
    }
}
