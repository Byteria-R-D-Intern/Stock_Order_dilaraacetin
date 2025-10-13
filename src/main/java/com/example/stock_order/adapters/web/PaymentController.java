package com.example.stock_order.adapters.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stock_order.adapters.web.dto.payment.ChargeRequest;
import com.example.stock_order.adapters.web.dto.payment.ChargeResponse;
import com.example.stock_order.adapters.web.dto.payment.TokenizeCardRequest;
import com.example.stock_order.adapters.web.dto.payment.TokenizeCardResponse;
import com.example.stock_order.application.AuditLogService;
import com.example.stock_order.application.PaymentService;
import com.example.stock_order.application.TokenizationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final TokenizationService tokenization;
    private final PaymentService payment;
    private final AuditLogService audit;

    @PostMapping("/tokenize")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TokenizeCardResponse> tokenize(@RequestBody @Valid TokenizeCardRequest req) {
        var rec = tokenization.tokenize(req.cardNumber(), req.expiryMonth(), req.expiryYear(), req.cvv());
        audit.log("TOKENIZE_CARD", "PAYMENT_TOKEN", null, null); // DİKKAT: PAN YOK
        return ResponseEntity.ok(new TokenizeCardResponse(rec.getToken(), rec.getLast4(), rec.getBrand(), rec.getExpiresAtEpochMs()));
    }

    @PostMapping("/charge")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChargeResponse> charge(@RequestBody @Valid ChargeRequest req) {
        var res = payment.charge(req.token(), req.amount(), req.currency());
        audit.log("PAYMENT_CHARGE", "PAYMENT", null, null);
        return ResponseEntity.ok(new ChargeResponse(res.chargeId(), res.status(), res.amount(), res.currency()));
    }
}