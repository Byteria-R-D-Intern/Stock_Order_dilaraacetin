package com.example.stock_order.adapters.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stock_order.adapters.web.dto.payment.SavedPaymentMethodResponse;
import com.example.stock_order.adapters.web.dto.payment.TokenizeCardRequest;
import com.example.stock_order.adapters.web.dto.payment.TokenizeCardResponse;
import com.example.stock_order.application.AuditLogService;
import com.example.stock_order.application.TokenizationService;
import com.example.stock_order.domain.ports.repository.UserRepository;
import com.example.stock_order.infrastructure.persistence.entity.SavedPaymentMethodEntity;
import com.example.stock_order.infrastructure.persistence.springdata.SavedPaymentMethodJpaRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final TokenizationService tokenization;
    private final AuditLogService audit;
    private final UserRepository users;
    private final SavedPaymentMethodJpaRepository savedRepo;

    private Long currentUserId(Authentication auth){
        String email = auth.getName();
        return users.findByEmail(email)
                .map(u -> u.getId())
                .orElseThrow(() -> new IllegalArgumentException("kullanıcı bulunamadı"));
    }

    @PostMapping("/tokenize")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TokenizeCardResponse> tokenize(Authentication auth,
                                                         @RequestBody @Valid TokenizeCardRequest req) {
        var rec = tokenization.tokenize(req.cardNumber(), req.expiryMonth(), req.expiryYear(), req.cvv());
        audit.log("TOKENIZE_CARD", "PAYMENT_TOKEN", null, null);

        if (req.save()) {
            var spm = new SavedPaymentMethodEntity();
            spm.setUserId(currentUserId(auth));
            spm.setToken(rec.token());
            spm.setLast4(rec.last4());
            spm.setBrand(rec.brand());
            spm.setExpiryMonth(req.expiryMonth());
            spm.setExpiryYear(req.expiryYear());
            spm.setActive(true);
            savedRepo.save(spm);
        }

        var body = new TokenizeCardResponse(rec.token(), rec.last4(), rec.brand(), rec.expiresAtEpochMs());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/methods")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SavedPaymentMethodResponse>> listMethods(Authentication auth) {
        var uid = currentUserId(auth);
        var list = savedRepo.findByUserIdAndActiveTrue(uid).stream()
                .map(e -> new SavedPaymentMethodResponse(
                        e.getId(),
                        e.getLast4(),
                        e.getBrand(),
                        e.getExpiryMonth(),
                        e.getExpiryYear()
                ))
                .toList();
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/methods/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteMethod(Authentication auth, @PathVariable Long id) {
        var uid = currentUserId(auth);
        var opt = savedRepo.findByIdAndUserIdAndActiveTrue(id, uid);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        var e = opt.get();
        e.setActive(false); 
        savedRepo.save(e);
        audit.log("PAYMENT_METHOD_DEACTIVATED", "PAYMENT_METHOD", e.getId(), java.util.Map.of("userId", uid));
        return ResponseEntity.noContent().build();
    }
}
