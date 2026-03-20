package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Order;
import com.example.demo.model.CartItem;
import com.example.demo.model.OrderDetail;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.OrderDetailRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.model.Product;
import com.example.demo.repository.IUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final IUserRepository userRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    @Transactional
    public Order placeOrder(Order order, List<CartItem> cartItems) {
        double total = 0;
        Order savedOrder = orderRepository.save(order);

        for (CartItem item : cartItems) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProduct().getId()));

            int quantity = item.getQuantity();

            double itemTotal = product.calculateTotalForQuantity(quantity);
            total += itemTotal;

            // Actually update the DB stock
            if (product.getPromotionStock() != null && product.getPromotionStock() > 0) {
                int promoUsed = Math.min(quantity, product.getPromotionStock());
                product.setPromotionStock(product.getPromotionStock() - promoUsed);
            }

            OrderDetail detail = new OrderDetail();
            detail.setOrder(savedOrder);
            detail.setProduct(product);
            detail.setQuantity(quantity);
            detail.setPrice(itemTotal / quantity); // Average price per unit for this order
            orderDetailRepository.save(detail);

            // Update traditional stock
            if (product.getStock() != null) {
                int newStock = product.getStock() - quantity;
                product.setStock(newStock < 0 ? 0 : newStock);
            }
            productRepository.save(product);
        }

        // Calculate Shipping Fee
        int totalQuantity = cartItems.stream().mapToInt(CartItem::getQuantity).sum();
        double shippingFee = 30000;
        if (total >= 1000000 && totalQuantity >= 2) {
            shippingFee = 0;
        }
        
        double finalTotal = total + shippingFee;
        int points = (int) (finalTotal / 15000 * 2);

        savedOrder.setTotalAmount(finalTotal);
        savedOrder.setShippingFee(shippingFee);
        savedOrder.setEarnedPoints(points);

        // Update User Loyalty Points if logged in
        if (savedOrder.getUser() != null) {
            com.example.demo.model.User user = savedOrder.getUser();
            int currentPoints = (user.getLoyaltyPoints() != null) ? user.getLoyaltyPoints() : 0;
            user.setLoyaltyPoints(currentPoints + points);
            userRepository.save(user);
        }

        return orderRepository.save(savedOrder);
    }

    public void deleteOrderById(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new IllegalStateException("Order with ID " + id + " does not exist.");
        }
        orderRepository.deleteById(id);
    }
}
