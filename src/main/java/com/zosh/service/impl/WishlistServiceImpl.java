package com.zosh.service.impl;

import com.zosh.exception.WishlistNotFoundException;
import com.zosh.model.Product;
import com.zosh.model.User;
import com.zosh.model.Wishlist;
import com.zosh.repository.WishlistRepository;
import com.zosh.service.WishlistService;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;

    @Override
    public Wishlist createWishlist(User user) {
        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        return wishlistRepository.save(wishlist);
    }

    @Override
    public Wishlist getWishlistByUserId(User user) {
        Wishlist wishlist = wishlistRepository.findByUserId(user.getId());
        if (wishlist == null) {
            wishlist = this.createWishlist(user);
        }
        return wishlist;
    }

    @Override
    public Wishlist addProductToWishlist(User user, Product product) throws WishlistNotFoundException {
        Wishlist wishlist = this.getWishlistByUserId(user);
        if (wishlist.getProducts().contains(product)) {
            wishlist.getProducts().remove(product);
        } else {
            wishlist.getProducts().add(product);
        }
        return wishlistRepository.save(wishlist);
    }

    @Override
    public Wishlist deleteProductFromWishlist(User user, Product product) throws WishlistNotFoundException {
        Wishlist wishlist = this.getWishlistByUserId(user);
        System.out.println(wishlist);
         Set<Product> products = wishlist.getProducts();
         Product prd= null;
         for (Iterator<Product> it = products.iterator(); it.hasNext(); ) {
        	 Product f = it.next();
             if(f.getId()==product.getId())
                 prd = f;
         }
        if(prd== null) {
            throw new WishlistNotFoundException("Product not found in wishlist");
        }
        products.remove(prd);
        wishlist.setProducts(products);
        return wishlistRepository.save(wishlist);
    }
}
