package com.example.stock_order.adapters.web.exception;

import java.time.Instant;

public class AccountLockedException extends RuntimeException {
    private final Instant lockedUntil;

    public AccountLockedException(Instant lockedUntil) {
        super("account locked until " + lockedUntil);
        this.lockedUntil = lockedUntil;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }
}