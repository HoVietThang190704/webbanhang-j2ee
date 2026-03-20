package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;

import com.example.demo.repository.OrderDetailRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public List<Product> getPromotionProducts() {
        return productRepository.findAll().stream()
                .filter(p -> p.getPromotionStock() != null && p.getPromotionStock() > 0)
                .toList();
    }

    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllProducts();
        }
        return productRepository.findByNameContainingIgnoreCase(keyword.trim());
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    public Product updateProduct(@NotNull Product product) {
        Product existingProduct = productRepository.findById(product.getId())
                .orElseThrow(() -> new IllegalStateException("Product with ID " +
                        product.getId() + " does not exist."));
        existingProduct.setName(product.getName());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setImage(product.getImage());
        existingProduct.setImagePath(product.getImagePath());
        existingProduct.setStock(product.getStock());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setDiscount(product.getDiscount());
        existingProduct.setOriginalPrice(product.getOriginalPrice());
        existingProduct.setPromotion(product.getPromotion());
        existingProduct.setPromotionStock(product.getPromotionStock());
        existingProduct.setStartPromotionStock(product.getStartPromotionStock());
        return productRepository.save(existingProduct);
    }

    public void deleteProductById(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalStateException("Product with ID " + id + " does not exist.");
        }
        
        // Check if product is referenced in any orders
        if (orderDetailRepository.findAll().stream().anyMatch(od -> od.getProduct() != null && od.getProduct().getId().equals(id))) {
             throw new IllegalStateException("Sản phẩm đã có trong đơn hàng, không thể xóa!");
        }
        
        productRepository.deleteById(id);
    }
}
