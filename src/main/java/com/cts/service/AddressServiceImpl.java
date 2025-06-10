package com.cts.service;

import com.cts.dto.*;
import com.cts.entity.Address;
import com.cts.entity.User;
import com.cts.exception.AddressNotFoundException;
import com.cts.exception.UserNotFoundException;
import com.cts.repository.AddressRepository;
import com.cts.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements IAddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public AddressResponse addAddress(int userId, AddressRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        Address address = modelMapper.map(request, Address.class);
        address.setUser(user);
        addressRepository.save(address);
        return modelMapper.map(address, AddressResponse.class);
    }

    @Override
    public AddressResponse updateAddress(UpdateAddressRequest request) {
        Address address = addressRepository.findById(request.getId())
            .orElseThrow(() -> new AddressNotFoundException("Address not found"));
        modelMapper.map(request, address);
        addressRepository.save(address);
        return modelMapper.map(address, AddressResponse.class);
    }

    @Override
    public void deleteAddress(int addressId) {
        if (!addressRepository.existsById(addressId)) {
            throw new AddressNotFoundException("Address not found");
        }
        addressRepository.deleteById(addressId);
    }

    @Override
    public List<AddressResponse> getUserAddresses(int userId) {
        return addressRepository.findByUserUserID(userId).stream()
            .map(address -> modelMapper.map(address, AddressResponse.class))
            .collect(Collectors.toList());
    }

    @Override
    public void setDefaultAddress(SetDefaultAddressRequest request) {
        List<Address> addresses = addressRepository.findByUserUserID(request.getUserId());
        for (Address addr : addresses) {
            addr.setDefault(addr.getId() == request.getAddressId());
        }
        addressRepository.saveAll(addresses);
    }
}
