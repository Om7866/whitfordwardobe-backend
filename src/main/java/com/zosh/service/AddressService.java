package com.zosh.service;

import java.util.List;

import com.zosh.model.Address;
import com.zosh.model.User;


public interface AddressService {
		
	 	Address updateAddress(Long id, Address updatedAddress, Long userId);
	    void deleteAddress(Long id, Long userId);
	    Address addAddress(Address newAddress, User user);
	    List<Address> getUserAddresses(Long userId);  // New method

	    
}
