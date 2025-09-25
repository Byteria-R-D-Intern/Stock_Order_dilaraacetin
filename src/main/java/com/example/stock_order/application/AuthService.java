package com.example.stock_order.application;

import java.time.Duration;
import java.time.Instant;

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

    public Long register(String email, String rawPassword) {
        users.findByEmail(email).ifPresent(u -> {
            throw new IllegalArgumentException("email already registered");
        });

        var u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setRole(User.Role.USER);
        u.setActive(true);
        u.setFailedLoginCount(0);
        var saved = users.save(u);
        return saved.getId();
    }

    public String login(String email, String rawPassword) {
        var user = users.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isActive()) {
            throw new IllegalStateException("user is disabled");
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            throw new AccountLockedException(user.getLockedUntil());
        }

        boolean ok = passwordEncoder.matches(rawPassword, user.getPasswordHash());
        if (!ok) {
            int failed = (user.getFailedLoginCount() == null ? 0 : user.getFailedLoginCount()) + 1;
            user.setFailedLoginCount(failed);
            if (failed >= MAX_FAILED) {
                user.setLockedUntil(Instant.now().plus(LOCK_DURATION));
                user.setFailedLoginCount(0); 
            }
            users.save(user);
            throw new InvalidCredentialsException();
        }

        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(Instant.now());
        users.save(user);

        return jwt.generateToken(String.valueOf(user.getId()), user.getEmail(), user.getRole().name());
    }
}
