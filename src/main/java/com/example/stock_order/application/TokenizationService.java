package com.example.stock_order.application;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.stock_order.util.CreditCardUtil;

@Service
public class TokenizationService {

    public record TokenRecord(String token, String last4, String brand, long expiresAtEpochMs) {}

    private record EncryptedCard(String cipherB64, String ivB64, String last4, String brand, Instant expiresAt) {}

    private final Map<String, EncryptedCard> store = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final Duration ttl;
    private final SecretKey secretKey;

    @Autowired
    public TokenizationService(
            @Value("${payment.token.ttl-seconds:60}") long ttlSeconds,
            @Value("${payment.token.secret:}") String base64Key
    ) {
        this.ttl = Duration.ofSeconds(ttlSeconds <= 0 ? 60 : ttlSeconds);
        byte[] keyBytes = decodeAesKey(base64Key);
        if (keyBytes == null) {
            keyBytes = new byte[32]; 
            random.nextBytes(keyBytes);
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    public TokenRecord tokenize(String pan, String expiryMonth, String expiryYear, String cvv) {
        if (pan == null || !pan.chars().allMatch(Character::isDigit) || pan.length() < 12)
            throw new IllegalArgumentException("invalid_pan");
        if (!CreditCardUtil.luhnValid(pan))
            throw new IllegalArgumentException("invalid_pan_luhn");

        String token = generateToken();

        EncryptedCard enc = encrypt(pan);
        store.put(token, enc);

        return new TokenRecord(token, enc.last4(), enc.brand(), enc.expiresAt().toEpochMilli());
    }

    public TokenRecord validateToken(String token) {
        EncryptedCard enc = store.get(token);
        if (enc == null) throw new IllegalArgumentException("invalid_token");
        if (enc.expiresAt().isBefore(Instant.now())) {
            store.remove(token);
            throw new IllegalArgumentException("token_expired");
        }
        return new TokenRecord(token, enc.last4(), enc.brand(), enc.expiresAt().toEpochMilli());
    }

    public void revoke(String token) { store.remove(token); }

    private byte[] decodeAesKey(String base64Key) {
        if (base64Key == null || base64Key.isBlank()) return null;
        byte[] keyBytes = null;
        try {
            keyBytes = Base64.getDecoder().decode(base64Key);
        } catch (IllegalArgumentException ignore) {
            try { keyBytes = Base64.getUrlDecoder().decode(base64Key); } catch (IllegalArgumentException ignore2) { }
        }
        if (keyBytes != null && (keyBytes.length == 16 || keyBytes.length == 24 || keyBytes.length == 32)) {
            return keyBytes;
        }
        return null;
    }

    private String generateToken() {
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        return "tok_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private EncryptedCard encrypt(String pan) {
        try {
            String last4 = pan.substring(Math.max(0, pan.length() - 4));
            String brand = CreditCardUtil.brandOf(pan);

            byte[] iv = new byte[12];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));
            byte[] cipherBytes = cipher.doFinal(pan.getBytes(StandardCharsets.UTF_8));

            Instant expiresAt = Instant.now().plus(ttl);
            return new EncryptedCard(
                    Base64.getEncoder().encodeToString(cipherBytes),
                    Base64.getEncoder().encodeToString(iv),
                    last4,
                    brand,
                    expiresAt
            );
        } catch (Exception e) {
            throw new IllegalStateException("encrypt_error", e);
        }
    }
}
