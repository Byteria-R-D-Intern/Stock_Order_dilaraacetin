package com.example.stock_order.application;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.stock_order.adapters.web.exception.AccountLockedException;
import com.example.stock_order.adapters.web.exception.InvalidCredentialsException;
import com.example.stock_order.config.JwtService;
import com.example.stock_order.domain.model.User;
import com.example.stock_order.domain.ports.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_FAILED = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final UserRepository users;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwt;
    private final AuditLogService audit;

    public Long register(String email, String rawPassword) {
        users.findByEmail(email).ifPresent(u -> { throw new IllegalArgumentException("email already registered"); });

        var u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setRole(User.Role.USER);
        u.setActive(true);
        u.setFailedLoginCount(0);
        var saved = users.save(u);

        audit.log("USER_REGISTERED", "USER", saved.getId(),
                Map.of("email", saved.getEmail(), "role", saved.getRole().name()));

        return saved.getId();
    }

    public String login(String email, String rawPassword) {
        var user = users.findByEmail(email)
                .orElseThrow(() -> { 
                    audit.log("LOGIN_FAILED", "USER", null, Map.of("email", email));
                    return new InvalidCredentialsException();
                });

        if (!user.isActive()) {
            audit.log("LOGIN_REJECTED_INACTIVE", "USER", user.getId(), Map.of("email", user.getEmail()));
            throw new IllegalStateException("user is disabled");
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            audit.log("LOGIN_REJECTED_LOCKED", "USER", user.getId(),
                    Map.of("email", user.getEmail(), "lockedUntil", user.getLockedUntil().toString()));
            throw new AccountLockedException(user.getLockedUntil());
        }

        boolean ok = passwordEncoder.matches(rawPassword, user.getPasswordHash());
        if (!ok) {
            int failed = (user.getFailedLoginCount() == null ? 0 : user.getFailedLoginCount()) + 1;
            user.setFailedLoginCount(failed);
            if (failed >= MAX_FAILED) {
                user.setLockedUntil(Instant.now().plus(LOCK_DURATION));
                user.setFailedLoginCount(0);
                users.save(user);
                audit.log("ACCOUNT_LOCKED", "USER", user.getId(),
                        Map.of("email", user.getEmail(), "lockedUntil", user.getLockedUntil().toString()));
                throw new InvalidCredentialsException();
            }
            users.save(user);
            audit.log("LOGIN_FAILED", "USER", user.getId(), Map.of("email", user.getEmail(), "failedCount", failed));
            throw new InvalidCredentialsException();
        }

        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(Instant.now());
        users.save(user);

        audit.log("LOGIN_SUCCESS", "USER", user.getId(), Map.of("email", user.getEmail()));

        return jwt.generateToken(String.valueOf(user.getId()), user.getEmail(), user.getRole().name());
    }
}
