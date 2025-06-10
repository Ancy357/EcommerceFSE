package com.cts.controller;

import com.cts.dto.*;
import com.cts.service.IAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final IAddressService addressService;

    @PostMapping("/{userId}/add")
    public ResponseEntity<AddressResponse> addAddress(@PathVariable int userId, @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.addAddress(userId, request));
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<AddressResponse> updateAddress(@PathVariable int userId, @RequestBody UpdateAddressRequest request) {
        return ResponseEntity.ok(addressService.updateAddress(request));
    }

    @DeleteMapping("/{addressId}/delete")
    public ResponseEntity<Void> deleteAddress(@PathVariable int addressId) {
        addressService.deleteAddress(addressId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<AddressResponse>> getUserAddresses(@PathVariable int userId) {
        return ResponseEntity.ok(addressService.getUserAddresses(userId));
    }

    @PutMapping("/set-default")
    public ResponseEntity<Void> setDefaultAddress(@RequestBody SetDefaultAddressRequest request) {
        addressService.setDefaultAddress(request);
        return ResponseEntity.ok().build();
    }
}
