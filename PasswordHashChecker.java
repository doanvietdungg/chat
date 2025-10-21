import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashChecker {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2.1seHllHvEyu";
        
        // Test common passwords
        String[] passwords = {
            "123456",
            "password",
            "admin",
            "test",
            "123",
            "password123",
            "admin123",
            "test123",
            "qwerty",
            "12345678",
            "abc123",
            "letmein",
            "welcome",
            "monkey",
            "dragon"
        };
        
        System.out.println("Testing hash: " + hash);
        System.out.println("==========================================");
        
        for (String password : passwords) {
            boolean matches = encoder.matches(password, hash);
            System.out.println("Password: '" + password + "' -> " + (matches ? "MATCH!" : "No match"));
            if (matches) {
                System.out.println("*** FOUND THE PASSWORD: " + password + " ***");
                break;
            }
        }
    }
}