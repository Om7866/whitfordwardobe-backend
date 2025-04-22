	package com.zosh.controller;
	
	import com.zosh.exception.UserException;
	import com.zosh.model.*;
	import com.zosh.repository.AddressRepository;
	import com.zosh.service.*;
	import lombok.RequiredArgsConstructor;
	import org.springframework.http.*;
	import org.springframework.web.bind.annotation.*;
	
	import java.util.*;
	
	@RestController
	@RequestMapping("/api/addresses")
	@RequiredArgsConstructor
	public class AddressController {
	
	    private final AddressService addressService;
	    private final UserService userService;
	    private final CartService cartService;
	    private final OrderService orderService;
	    private final AddressRepository addressRepository;
	
	    // ✅ Add New Address
	    @PostMapping
	    public ResponseEntity<Address> addAddress(
	            @RequestBody Address newAddress,
	            @RequestHeader("Authorization") String jwt
	    ) {
	        try {
	            User user = userService.findUserProfileByJwt(jwt);
	            Address savedAddress = addressService.addAddress(newAddress, user);
	            System.out.println("savedAddress" + savedAddress);
	            return new ResponseEntity<>(savedAddress, HttpStatus.CREATED);
	        } catch (UserException e) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	        }
	    }
	
	    // ✅ Update Existing Address
	    @PutMapping("/{id}")
	    public ResponseEntity<Address> updateAddress(
	            @PathVariable Long id,
	            @RequestBody Address updatedAddress,
	            @RequestHeader("Authorization") String jwt
	    ) {
	        try {
	            User user = userService.findUserProfileByJwt(jwt);
	            Address address = addressService.updateAddress(id, updatedAddress, user.getId());
	            return ResponseEntity.ok(address);
	        } catch (IllegalArgumentException e) {
	            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
	        } catch (UserException e) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	        }
	    }
	
	    // ✅ Delete Address
	    @DeleteMapping("/{id}")
	    public ResponseEntity<String> deleteAddress(
	            @PathVariable Long id,
	            @RequestHeader("Authorization") String jwt
	    ) {
	        try {
	            User user = userService.findUserProfileByJwt(jwt);
	            addressService.deleteAddress(id, user.getId());
	            return ResponseEntity.ok("Address deleted successfully.");
	        } catch (UserException e) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
	        } catch (Exception e) {
	            e.printStackTrace(); 
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete address.");
	        }
	    }
	    
	    
	    @GetMapping()
	    public ResponseEntity<?> getUserAddresses(@RequestHeader("Authorization") String jwt) {
	        try {
	            User user = userService.findUserProfileByJwt(jwt);
	            List<Address> addresses = addressService.getUserAddresses(user.getId());
	            System.out.println("addresses " + addresses);
	            return ResponseEntity.ok(addresses);
	        } catch (UserException e) {
	            return ResponseEntity.status(404).body("User not found");
	        } catch (Exception e) {
	            e.printStackTrace(); // Log the error
	            return ResponseEntity.status(500).body("Error: " + e.getMessage());
	        }
	    }
	   
	}
	
