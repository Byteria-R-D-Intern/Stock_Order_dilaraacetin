package com.example.stock_order.adapters.web.dto.auth;

public record AuthResponse(
        String token,
        String tokenType,  
        String role        
) {}