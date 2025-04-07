package com.zosh.service.impl;

import com.zosh.exception.CouponNotValidException;
import com.zosh.model.Cart;
import com.zosh.model.Coupon;
import com.zosh.model.User;
import com.zosh.repository.CartRepository;
import com.zosh.repository.CouponRepository;
import com.zosh.repository.UserRepository;
import com.zosh.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;

    @Override
    public Cart applyCoupon(String code, double orderValue, User user) throws Exception {
        Coupon coupon = couponRepository.findByCode(code);
        Cart cart = cartRepository.findByUserId(user.getId());

        if (coupon == null) {
            throw new CouponNotValidException("Coupon not found.");
        }
        if (user.getUsedCoupons().contains(coupon)) {
            throw new CouponNotValidException("Coupon already used.");
        }
        if (cart.getTotalSellingPrice() < coupon.getMinimumOrderValue()) {
            throw new CouponNotValidException("Valid for minimum order value " + coupon.getMinimumOrderValue());
        }

        if (coupon.isActive() && 
            LocalDate.now().isAfter(coupon.getValidityStartDate()) &&
            LocalDate.now().isBefore(coupon.getValidityEndDate())) {

            user.getUsedCoupons().add(coupon);
            userRepository.save(user);

            // Apply discount correctly
            double discountAmount = (cart.getTotalSellingPrice() * coupon.getDiscountPercentage()) / 100;
            double newTotal = cart.getTotalSellingPrice() - discountAmount;

            // Ensure total is not negative
            newTotal = Math.max(newTotal, 0);

            cart.setTotalSellingPrice(newTotal);
            cart.setCouponCode(code);
            cart.setCouponPrice(discountAmount);

            return cartRepository.save(cart);
        }

        throw new CouponNotValidException("Coupon not valid.");
    }

    @Override
    public Cart removeCoupon(String code, User user) throws Exception {
        Coupon coupon = couponRepository.findByCode(code);

        if (coupon == null) {
            throw new Exception("Coupon not found.");
        }

        user.getUsedCoupons().remove(coupon);
        Cart cart = cartRepository.findByUserId(user.getId());

        // Recalculate original price before discount
        double originalPrice = cart.getTotalSellingPrice() + cart.getCouponPrice();

        cart.setTotalSellingPrice(originalPrice);
        cart.setCouponCode(null);
        cart.setCouponPrice(0);

        return cartRepository.save(cart);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Coupon createCoupon(Coupon coupon) {
        return couponRepository.save(coupon);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCoupon(Long couponId) {
        couponRepository.deleteById(couponId);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    @Override
    public Coupon getCouponById(Long couponId) {
        return couponRepository.findById(couponId).orElse(null);
    }
}
