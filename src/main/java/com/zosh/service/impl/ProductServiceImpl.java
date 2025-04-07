package com.zosh.service.impl;

import com.zosh.exception.ProductException;
import com.zosh.model.Category;
import com.zosh.model.Product;
import com.zosh.model.Seller;
import com.zosh.repository.CategoryRepository;
import com.zosh.repository.ProductRepository;
import com.zosh.request.CreateProductRequest;
import com.zosh.service.ProductService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public Product createProduct(CreateProductRequest req, Seller seller) throws ProductException {
        int discountPercentage = calculateDiscountPercentage(req.getMrpPrice(), req.getSellingPrice());

        Category category1 = categoryRepository.findByCategoryId(req.getCategory());
        if (category1 == null) {
            Category category = new Category();
            category.setCategoryId(req.getCategory());
            category.setLevel(1);
            category.setName(req.getCategory().replace("_", " "));
            category1 = categoryRepository.save(category);
        }

        String category2Id = Optional.ofNullable(req.getCategory2()).orElse("Unknown");
        Category category2 = categoryRepository.findByCategoryId(category2Id);
        if (category2 == null) {
            Category category = new Category();
            category.setCategoryId(category2Id);
            category.setLevel(2);
            category.setParentCategory(category1);
            category.setName(category2Id.replace("_", " "));
            category2 = categoryRepository.save(category);
        }

        String category3Id = Optional.ofNullable(req.getCategory3()).orElse("Unknown");
        Category category3 = categoryRepository.findByCategoryId(category3Id);
        if (category3 == null) {
            Category category = new Category();
            category.setCategoryId(category3Id);
            category.setLevel(3);
            category.setParentCategory(category2);
            category.setName(category3Id.replace("_", " "));
            category3 = categoryRepository.save(category);
        }

        Product product = new Product();
        product.setSeller(seller);
        product.setCategory(category3);
        product.setTitle(req.getTitle());
        product.setColor(req.getColor());
        product.setDescription(req.getDescription());
        product.setDiscountPercent(discountPercentage);
        product.setSellingPrice(req.getSellingPrice());
        product.setImages(req.getImages());
        product.setMrpPrice(req.getMrpPrice());
        product.setSizes(req.getSizes());
        product.setCreatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }

    public static int calculateDiscountPercentage(double mrpPrice, double sellingPrice) {
        if (mrpPrice <= 0) {
            throw new IllegalArgumentException("Actual price must be greater than zero.");
        }
        double discount = mrpPrice - sellingPrice;
        double discountPercentage = (discount / mrpPrice) * 100;
        return (int) discountPercentage;
    }

    @Override
    public void deleteProduct(Long productId) throws ProductException {
        Product product = findProductById(productId);
        productRepository.delete(product);
    }

    @Override
    public Product updateProduct(Long productId, Product product) throws ProductException {
        findProductById(productId);
        product.setId(productId);
        return productRepository.save(product);
    }

    @Override
    public Product findProductById(Long id) throws ProductException {
        return productRepository.findById(id).orElseThrow(() -> new ProductException("Product not found"));
    }

    @Override
    public List<Product> searchProduct(String query) {
        return productRepository.searchProduct(query);
    }

    @Override
    public List<Product> recentlyAddedProduct() {
        return List.of();
    }

    @Override
    public List<Product> getProductBySellerId(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }
    @Override
    public Page<Product> getAllProduct(String category, String brand, String color, String size, 
                                       Integer minPrice, Integer maxPrice, Integer minDiscount, 
                                       String sort, String stock, Integer pageNumber) {
        Specification<Product> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (category != null && !category.isEmpty()) {
                Join<Product, Category> categoryJoin = root.join("category");
                predicates.add(criteriaBuilder.equal(categoryJoin.get("categoryId"), category));
            }

            if (color != null && !color.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("color"), color));
            }

            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("sellingPrice"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("sellingPrice"), maxPrice));
            }

            if (minDiscount != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("discountPercent"), minDiscount));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // ✅ Define sorting based on the 'sort' parameter
        Sort sorting = Sort.unsorted(); // Default sorting

        if (sort != null && !sort.isEmpty()) {
            switch (sort) {
                case "price_low" -> sorting = Sort.by("sellingPrice").ascending();
                case "price_high" -> sorting = Sort.by("sellingPrice").descending();
                default -> sorting = Sort.unsorted();
            }
        }

        // ✅ Apply pagination with sorting
        Pageable pageable = PageRequest.of((pageNumber != null ? pageNumber : 0), 10, sorting);

        return productRepository.findAll(spec, pageable);
    }

    	
    }
