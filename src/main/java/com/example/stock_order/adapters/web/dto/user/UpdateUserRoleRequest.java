package com.example.stock_order.adapters.web.dto.user;

import com.example.stock_order.domain.model.User;

import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(@NotNull User.Role role) {}