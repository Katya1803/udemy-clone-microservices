package com.app.auth.util;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

/**
 * Utility to generate RSA key pairs for JWT signing
 * Run this ONCE to generate keys, then store them securely
 *
 * Usage:
 * 1. Run main() to generate keys
 * 2. Copy private key to auth-service environment (JWT_PRIVATE_KEY)
 * 3. Copy public key to ALL services environment (JWT_PUBLIC_KEY)
 * 4. NEVER commit these keys to git
 */
public class RSAKeyPairGenerator {

    public static void main(String[] args) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); // 2048-bit RSA (industry standard)

        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        String privateKeyPEM = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        String publicKeyPEM = Base64.getEncoder().encodeToString(publicKey.getEncoded());

        System.out.println("=".repeat(80));
        System.out.println("RSA KEY PAIR GENERATED");
        System.out.println("=".repeat(80));
        System.out.println();

        System.out.println("PRIVATE KEY (auth-service ONLY):");
        System.out.println("-".repeat(80));
        System.out.println(privateKeyPEM);
        System.out.println();

        System.out.println("PUBLIC KEY (all services):");
        System.out.println("-".repeat(80));
        System.out.println(publicKeyPEM);
        System.out.println();

        System.out.println("=".repeat(80));
        System.out.println("ADD TO ENVIRONMENT VARIABLES:");
        System.out.println("=".repeat(80));
        System.out.println();
        System.out.println("# auth-service only (in .env or environment)");
        System.out.println("JWT_PRIVATE_KEY=" + privateKeyPEM);
        System.out.println();
        System.out.println("# All services (in .env or environment)");
        System.out.println("JWT_PUBLIC_KEY=" + publicKeyPEM);
        System.out.println();
        System.out.println("⚠️  SECURITY WARNING:");
        System.out.println("- NEVER commit these keys to Git");
        System.out.println("- Store private key ONLY in auth-service");
        System.out.println("- Rotate keys periodically (every 6-12 months)");
        System.out.println("=".repeat(80));

        // Optional: Save to files (for reference, don't commit these)
        Files.write(Paths.get("private_key.txt"), privateKeyPEM.getBytes());
        Files.write(Paths.get("public_key.txt"), publicKeyPEM.getBytes());

        System.out.println();
        System.out.println("✅ Keys also saved to private_key.txt and public_key.txt");
        System.out.println("   (Remember to delete these files after copying to environment)");
    }
}