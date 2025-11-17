package com.app.auth.util;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;


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

    }
}