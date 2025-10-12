package com.example.stock_order.application;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.stock_order.util.CreditCardUtil;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenizationService {

    private final Map<String, TokenRecord> tokenStore = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    @Value("${payment.token.ttl-minutes:30}")
    private long tokenTtlMinutes;

    @Getter
    public static class TokenRecord {
        private final String token;
        private final String last4;
        private final String brand;
        private final long expiresAtEpochMs;

        private TokenRecord(String token, String last4, String brand, long expiresAtEpochMs) {
            this.token = token;
            this.last4 = last4;
            this.brand = brand;
            this.expiresAtEpochMs = expiresAtEpochMs;
        }
        public static TokenRecord of(String token, String pan) {
            String last4 = pan.substring(pan.length()-4);
            String brand = CreditCardUtil.brandOf(pan);
            long expMs = Instant.now().plus(Duration.ofMinutes(30)).toEpochMilli();
            return new TokenRecord(token, last4, brand, expMs);
        }
        public static TokenRecord of(String token, String pan, long ttlMin) {
            String last4 = pan.substring(pan.length()-4);
            String brand = CreditCardUtil.brandOf(pan);
            long expMs = Instant.now().plus(Duration.ofMinutes(ttlMin)).toEpochMilli();
            return new TokenRecord(token, last4, brand, expMs);
        }
        public boolean expired() { return Instant.now().toEpochMilli() > expiresAtEpochMs; }
    }

    public TokenRecord tokenize(String pan, String expiryMonth, String expiryYear, String cvv) {
        if (!CreditCardUtil.luhnValid(pan)) {
            throw new IllegalArgumentException("geçersiz kart numarası");
        }
        String token = generateToken();
        TokenRecord rec = TokenRecord.of(token, pan, tokenTtlMinutes);
        tokenStore.put(token, rec);
        return rec;
    }

    public TokenRecord validateToken(String token) {
        TokenRecord rec = tokenStore.get(token);
        if (rec == null) throw new IllegalArgumentException("geçersiz token");
        if (rec.expired()) {
            tokenStore.remove(token);
            throw new IllegalArgumentException("token süresi doldu");
        }
        return rec;
    }

    public int purgeExpired() {
        int removed = 0;
        for (var e : tokenStore.entrySet()) {
            if (e.getValue().expired()) {
                tokenStore.remove(e.getKey());
                removed++;
            }
        }
        return removed;
    }

    private String generateToken() {
        byte[] buf = new byte[18];
        random.nextBytes(buf);
        return "tok_" + Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}