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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stock_order.adapters.web.dto.profile.AddressCreateRequest;
import com.example.stock_order.adapters.web.dto.profile.AddressResponse;
import com.example.stock_order.adapters.web.dto.profile.AddressUpdateRequest;
import com.example.stock_order.adapters.web.dto.profile.UpdateProfileRequest;
import com.example.stock_order.adapters.web.exception.NotFoundException;
import com.example.stock_order.application.AuditLogService;
import com.example.stock_order.application.NotificationService;
import com.example.stock_order.domain.model.User;
import com.example.stock_order.domain.model.UserAddress;
import com.example.stock_order.domain.ports.repository.UserAddressRepository;
import com.example.stock_order.domain.ports.repository.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Validated
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private final UserRepository users;
    private final UserAddressRepository addresses;
    private final AuditLogService audit;
    private final NotificationService notifications;

    private Long currentUserId(Authentication auth){
        String email = auth.getName();
        return users.findByEmail(email).map(User::getId)
                .orElseThrow(() -> new IllegalArgumentException("kullanıcı bulunamadı"));
    }

    @PutMapping
    public ResponseEntity<Void> updateProfile(Authentication auth, @RequestBody @Valid UpdateProfileRequest req){
        var uid = currentUserId(auth);
        var u = users.findById(uid).orElseThrow(() -> new NotFoundException("user not found"));
        if (req.phoneNumber() != null) u.setPhoneNumber(req.phoneNumber());
        users.save(u);
        audit.log("PROFILE_UPDATED", "USER", uid, java.util.Map.of("phone", u.getPhoneNumber()));

        notifications.notifyAccount(uid, "Profile updated", "Your profile information was updated.");

        return ResponseEntity.ok().build();
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressResponse>> listAddresses(Authentication auth){
        var uid = currentUserId(auth);
        var list = addresses.findByUserId(uid).stream().map(AddressResponse::of).toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/addresses")
    public ResponseEntity<AddressResponse> createAddress(Authentication auth, @RequestBody @Valid AddressCreateRequest req){
        var uid = currentUserId(auth);

        if (req.isDefault()) {
            addresses.clearDefaultForUser(uid);
        }

        UserAddress a = new UserAddress();
        a.setUserId(uid);
        a.setTitle(req.title());
        a.setRecipientName(req.recipientName());
        a.setLine1(req.line1());
        a.setLine2(req.line2());
        a.setCity(req.city());
        a.setState(req.state());
        a.setPostalCode(req.postalCode());
        a.setCountry(req.country());
        a.setPhone(req.phone());
        a.setDefault(req.isDefault());

        var saved = addresses.save(a);
        audit.log("ADDRESS_CREATED", "USER_ADDRESS", saved.getId(), java.util.Map.of("userId", uid, "title", saved.getTitle()));

        notifications.notifyAccount(uid, "Address added", "Address \"" + saved.getTitle() + "\" has been added.");

        return ResponseEntity.ok(AddressResponse.of(saved));
    }

    @PutMapping("/addresses/{id}")
    public ResponseEntity<AddressResponse> updateAddress(Authentication auth, @PathVariable Long id, @RequestBody @Valid AddressUpdateRequest req){
        var uid = currentUserId(auth);
        var a = addresses.findByIdAndUserId(id, uid).orElseThrow(() -> new NotFoundException("address not found"));

        if (req.title() != null)         a.setTitle(req.title());
        if (req.recipientName() != null) a.setRecipientName(req.recipientName());
        if (req.line1() != null)         a.setLine1(req.line1());
        if (req.line2() != null)         a.setLine2(req.line2());
        if (req.city() != null)          a.setCity(req.city());
        if (req.state() != null)         a.setState(req.state());
        if (req.postalCode() != null)    a.setPostalCode(req.postalCode());
        if (req.country() != null)       a.setCountry(req.country());
        if (req.phone() != null)         a.setPhone(req.phone());

        if (req.isDefault() != null) {
            if (req.isDefault()) {
                addresses.clearDefaultForUser(uid);
                a.setDefault(true);
            } else {
                a.setDefault(false);
            }
        }

        var saved = addresses.save(a);
        audit.log("ADDRESS_UPDATED", "USER_ADDRESS", saved.getId(), java.util.Map.of("userId", uid, "title", saved.getTitle()));

        notifications.notifyAccount(uid, "Address updated", "Address \"" + saved.getTitle() + "\" has been updated.");

        return ResponseEntity.ok(AddressResponse.of(saved));
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<Void> deleteAddress(Authentication auth, @PathVariable Long id){
        var uid = currentUserId(auth);
        var a = addresses.findByIdAndUserId(id, uid).orElseThrow(() -> new NotFoundException("address not found"));
        addresses.deleteByIdAndUserId(id, uid);
        audit.log("ADDRESS_DELETED", "USER_ADDRESS", id, java.util.Map.of("userId", uid));

        notifications.notifyAccount(uid, "Address deleted", "Address \"" + a.getTitle() + "\" has been deleted.");

        return ResponseEntity.ok().build();
    }

    @PutMapping("/addresses/{id}/default")
    public ResponseEntity<Void> makeDefault(Authentication auth, @PathVariable Long id){
        var uid = currentUserId(auth);
        var a = addresses.findByIdAndUserId(id, uid).orElseThrow(() -> new NotFoundException("address not found"));
        addresses.clearDefaultForUser(uid);
        a.setDefault(true);
        addresses.save(a);
        audit.log("ADDRESS_SET_DEFAULT", "USER_ADDRESS", id, java.util.Map.of("userId", uid));

        notifications.notifyAccount(uid, "Default address set", "Default address changed to \"" + a.getTitle() + "\".");

        return ResponseEntity.ok().build();
    }
}
