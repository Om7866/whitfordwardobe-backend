package com.zosh.service.impl;

import com.zosh.model.Address;
import com.zosh.model.User;
import com.zosh.repository.AddressRepository;
import com.zosh.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Address> getUserAddresses(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public Address updateAddress(Long id, Address updatedAddress, Long userId) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));

        if (!address.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized to update this address");
        }

        address.setName(updatedAddress.getName());
        address.setAddress(updatedAddress.getAddress());
        address.setLocality(updatedAddress.getLocality());
        address.setCity(updatedAddress.getCity());
        address.setState(updatedAddress.getState());
        address.setPinCode(updatedAddress.getPinCode());
        address.setMobile(updatedAddress.getMobile());

        return addressRepository.save(address);
    }

    @Override
    @Transactional
    public void deleteAddress(Long id, Long userId) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));

        if (!address.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized to delete this address");
        }

        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public Address addAddress(Address address, User user) {
        address.setUser(user);
        return addressRepository.save(address);
    }
}