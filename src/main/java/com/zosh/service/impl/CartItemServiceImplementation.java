package com.zosh.service.impl;

import com.zosh.exception.CartItemException;
import com.zosh.exception.UserException;
import com.zosh.model.Cart;
import com.zosh.model.CartItem;
import com.zosh.model.User;
import com.zosh.repository.CartItemRepository;
import com.zosh.service.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartItemServiceImplementation implements CartItemService {
    
    private final CartItemRepository cartItemRepository;

    @Autowired
    public CartItemServiceImplementation(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public CartItem updateCartItem(Long userId, Long id, CartItem cartItem)
            throws CartItemException, UserException {
        
        CartItem item = findCartItemById(id);

        if (item.getCart() == null || item.getCart().getUser() == null) {
            throw new CartItemException("Cart or user associated with this item is null");
        }

        User cartItemUser = item.getCart().getUser();

        if (cartItemUser.getId().equals(userId)) {
            item.setQuantity(cartItem.getQuantity());
            item.setMrpPrice(item.getQuantity() * item.getProduct().getMrpPrice());
            item.setSellingPrice(item.getQuantity() * item.getProduct().getSellingPrice());
            
            return cartItemRepository.save(item);
        } else {
            throw new CartItemException("You can't update another user's cart item");
        }
    }

    @Override
    public void removeCartItem(Long userId, Long cartItemId)
            throws CartItemException, UserException {

        System.out.println("userId- " + userId + " cartItemId " + cartItemId);

        CartItem cartItem = findCartItemById(cartItemId);

        if (cartItem.getCart() == null || cartItem.getCart().getUser() == null) {
            throw new CartItemException("Cart or user associated with this item is null");
        }

        User cartItemUser = cartItem.getCart().getUser();

        if (cartItemUser.getId().equals(userId)) {
            cartItemRepository.deleteById(cartItem.getId());
        } else {
            throw new UserException("You can't remove another user's item");
        }
    }

    @Override
    public CartItem findCartItemById(Long cartItemId) throws CartItemException {
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartItemException("CartItem not found with id: " + cartItemId));
    }
}
