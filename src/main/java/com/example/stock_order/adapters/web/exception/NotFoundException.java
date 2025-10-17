package com.example.stock_order.adapters.web.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException() { super("not_found"); }
    public NotFoundException(String message) { super(message); }
}
