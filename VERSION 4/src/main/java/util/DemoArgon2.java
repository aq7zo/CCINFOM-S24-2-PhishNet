package util;

/**
 * Demo class to demonstrate Argon2 password hashing functionality.
 * This can be run standalone to show how Argon2 works in the PhishNet system.
 * 
 * Usage: Run this class to see Argon2 hashing and verification in action.
 */
public class DemoArgon2 {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  PhishNet Argon2 Password Hashing Demo");
        System.out.println("========================================\n");
        
        // Demo 1: Basic Password Hashing
        System.out.println("DEMO 1: Basic Password Hashing");
        System.out.println("----------------------------------------");
        String password1 = "SecurePassword123!";
        System.out.println("Original Password: " + password1);
        
        String hash1 = SecurityUtils.hashPassword(password1);
        if (hash1 != null) {
            System.out.println("Generated Hash: " + hash1);
            System.out.println("Hash Length: " + hash1.length() + " characters");
            System.out.println("Hash Format: " + (hash1.startsWith("$argon2id$") ? "✓ Argon2id" : "✗ Invalid"));
            System.out.println();
        } else {
            System.out.println("✗ Hashing failed!");
            return;
        }
        
        // Demo 2: Password Verification
        System.out.println("DEMO 2: Password Verification");
        System.out.println("----------------------------------------");
        System.out.println("Testing password: " + password1);
        System.out.println("Against hash: " + hash1.substring(0, 50) + "...");
        
        boolean verified1 = SecurityUtils.verifyPassword(password1, hash1);
        System.out.println("Verification Result: " + (verified1 ? "✓ SUCCESS" : "✗ FAILED"));
        System.out.println();
        
        // Demo 3: Wrong Password Rejection
        System.out.println("DEMO 3: Wrong Password Rejection");
        System.out.println("----------------------------------------");
        String wrongPassword = "WrongPassword123!";
        System.out.println("Testing wrong password: " + wrongPassword);
        System.out.println("Against hash: " + hash1.substring(0, 50) + "...");
        
        boolean verified2 = SecurityUtils.verifyPassword(wrongPassword, hash1);
        System.out.println("Verification Result: " + (verified2 ? "✗ FAILED (Security Breach!)" : "✓ CORRECTLY REJECTED"));
        System.out.println();
        
        // Demo 4: Salt Uniqueness (Same Password, Different Hashes)
        System.out.println("DEMO 4: Salt Uniqueness");
        System.out.println("----------------------------------------");
        String samePassword = "SamePassword123!";
        System.out.println("Password: " + samePassword);
        
        String hash2 = SecurityUtils.hashPassword(samePassword);
        String hash3 = SecurityUtils.hashPassword(samePassword);
        
        System.out.println("Hash 1: " + hash2.substring(0, 60) + "...");
        System.out.println("Hash 2: " + hash3.substring(0, 60) + "...");
        System.out.println("Are hashes identical? " + (hash2.equals(hash3) ? "Yes ✗ (Problem!)" : "No ✓ (Correct!)"));
        System.out.println("Both verify correctly? " + 
            (SecurityUtils.verifyPassword(samePassword, hash2) && 
             SecurityUtils.verifyPassword(samePassword, hash3) ? "✓ Yes" : "✗ No"));
        System.out.println();
        
        // Demo 5: Hash Format Breakdown
        System.out.println("DEMO 5: Hash Format Breakdown");
        System.out.println("----------------------------------------");
        System.out.println("Full Hash: " + hash1);
        System.out.println();
        System.out.println("Format Breakdown:");
        if (hash1.startsWith("$argon2id$")) {
            String[] parts = hash1.split("\\$");
            System.out.println("  Variant: " + parts[1]);
            System.out.println("  Version: " + parts[2]);
            System.out.println("  Parameters: " + parts[3]);
            System.out.println("  Salt: " + parts[4].substring(0, Math.min(20, parts[4].length())) + "...");
            System.out.println("  Hash: " + parts[5].substring(0, Math.min(20, parts[5].length())) + "...");
        }
        System.out.println();
        
        // Demo 6: Performance Test
        System.out.println("DEMO 6: Performance Test");
        System.out.println("----------------------------------------");
        String testPassword = "PerformanceTest123!";
        int iterations = 5;
        
        System.out.println("Hashing password " + iterations + " times...");
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < iterations; i++) {
            SecurityUtils.hashPassword(testPassword);
        }
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double avgTime = (double) totalTime / iterations;
        
        System.out.println("Total Time: " + totalTime + " ms");
        System.out.println("Average Time per Hash: " + String.format("%.2f", avgTime) + " ms");
        System.out.println("Performance: " + (avgTime < 500 ? "✓ Good" : "⚠ Slow"));
        System.out.println();
        
        // Demo 7: Security Features Summary
        System.out.println("DEMO 7: Security Features Summary");
        System.out.println("----------------------------------------");
        System.out.println("✓ Passwords are never stored in plaintext");
        System.out.println("✓ Each password gets a unique salt");
        System.out.println("✓ Uses Argon2id (industry best practice)");
        System.out.println("✓ Resistant to GPU and side-channel attacks");
        System.out.println("✓ Self-contained hash format (includes parameters)");
        System.out.println("✓ Constant-time comparison (prevents timing attacks)");
        System.out.println();
        
        System.out.println("========================================");
        System.out.println("  Demo Complete!");
        System.out.println("========================================");
    }
}


