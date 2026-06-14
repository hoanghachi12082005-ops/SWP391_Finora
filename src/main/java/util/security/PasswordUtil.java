//package       util.security;
//
//import java.nio.charset.StandardCharsets;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//
//public class PasswordUtil {
//    public static String hash(String rawPassword) {
//        if (rawPassword == null) return null;
//        try {
//            MessageDigest digest = MessageDigest.getInstance("SHA-256");
//            byte[] encoded = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
//            StringBuilder sb = new StringBuilder();
//            for (byte b : encoded) sb.append(String.format("%02x", b));
//            return sb.toString();
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public static boolean matches(String rawPassword, String hashedPassword) {
//        return hash(rawPassword).equals(hashedPassword);
//    }
//}
