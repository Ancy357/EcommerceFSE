package com.cts.controller;

import com.cts.dto.*;
import com.cts.service.IAddressService;
import jakarta.validation.Valid; // Import the @Valid annotation
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final IAddressService addressService;

    @PostMapping("/{userId}/add")
    public ResponseEntity<AddressResponse> addAddress(
            @PathVariable int userId,
            @Valid @RequestBody AddressRequest request) { // Added @Valid
        return ResponseEntity.ok(addressService.addAddress(userId, request));
    }

    @PutMapping("/update/{userId}") // Consider if userId in path is necessary if UpdateAddressRequest already has an ID.
                                    // If userId from path is for authorization/context, keep it.
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable int userId,
            @Valid @RequestBody UpdateAddressRequest request) { // Added @Valid
        // You might want to pass userId to the service method if it's relevant for authorization or to ensure the user owns the address.
        return ResponseEntity.ok(addressService.updateAddress(request));
    }

    @DeleteMapping("/{addressId}/delete") // No @Valid needed for @PathVariable
    public ResponseEntity<Void> deleteAddress(@PathVariable int addressId) {
        addressService.deleteAddress(addressId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}") // No @Valid needed for @PathVariable
    public ResponseEntity<List<AddressResponse>> getUserAddresses(@PathVariable int userId) {
        return ResponseEntity.ok(addressService.getUserAddresses(userId));
    }

    @PutMapping("/set-default")
    public ResponseEntity<Void> setDefaultAddress(@Valid @RequestBody SetDefaultAddressRequest request) { // Added @Valid
        addressService.setDefaultAddress(request);
        return ResponseEntity.ok().build();
    }
}