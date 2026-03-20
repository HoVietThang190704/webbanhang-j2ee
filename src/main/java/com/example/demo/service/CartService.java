package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import com.example.demo.model.CartItem;
import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;

@Service
@SessionScope
public class CartService {
  private List<CartItem> cartItems = new ArrayList<>();

  @Autowired
  private ProductRepository productRepository;

  public void addToCart(Long productId, int quantity) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
    cartItems.add(new CartItem(product, quantity));
  }

  public List<CartItem> getCartItems() {
    return cartItems;
  }

  public void removeFromCart(Long productId) {
    cartItems.removeIf(item -> item.getProduct().getId().equals(productId));
  }

  public void clearCart() {
    cartItems.clear();
  }

  public void updateQuantity(Long productId, int quantity) {
    if (quantity <= 0) {
      removeFromCart(productId);
      return;
    }
    for (CartItem item : cartItems) {
      if (item.getProduct().getId().equals(productId)) {
        item.setQuantity(quantity);
        break;
      }
    }
  }
}