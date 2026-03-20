package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.CartItem;
import com.example.demo.model.Order;
import com.example.demo.service.CartService;
import com.example.demo.service.OrderService;
import com.example.demo.service.MoMoPaymentService;
import com.example.demo.service.UserService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MoMoPaymentService momoPaymentService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String showCart(Model model) {
        List<CartItem> items = cartService.getCartItems();
        double subtotal = items.stream().mapToDouble(i -> i.getProduct().calculateTotalForQuantity(i.getQuantity())).sum();
        int totalQty = items.stream().mapToInt(CartItem::getQuantity).sum();
        
        double shippingFee = 30000;
        if (subtotal >= 1000000 && totalQty >= 2) shippingFee = 0;
        
        model.addAttribute("cartItems", items);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("total", subtotal + shippingFee);
        model.addAttribute("earnedPoints", (int)((subtotal + shippingFee) / 15000 * 2));
        
        return "/cart/cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId, @RequestParam int quantity) {
        cartService.addToCart(productId, quantity);
        return "redirect:/cart";
    }

    @GetMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId) {
        cartService.removeFromCart(productId);
        return "redirect:/cart";
    }

    @GetMapping("/update/{productId}")
    public String updateCartQuantity(@PathVariable Long productId, @RequestParam("qty") int qty) {
        cartService.updateQuantity(productId, qty);
        return "redirect:/cart";
    }

    @GetMapping("/clear")
    public String clearCart() {
        cartService.clearCart();
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(
            @RequestParam("name") String customerName,
            @RequestParam("phone") String phone,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam("gender") String gender,
            @RequestParam("deliveryMethod") String deliveryMethod,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "paymentMethod", defaultValue = "cod") String paymentMethod,
            RedirectAttributes redirectAttributes,
            Model model) {

        List<CartItem> cartItems = cartService.getCartItems();
        if (cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Your cart is empty!");
            return "redirect:/cart";
        }

        Order order = new Order();
        order.setCustomerName(customerName);
        order.setPhone(phone);
        order.setEmail(email);
        order.setGender(gender);
        order.setDeliveryMethod(deliveryMethod);
        order.setAddress(address);
        order.setNotes(notes);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus("PENDING");

        // Link User if logged in
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            userService.findByUsername(username).ifPresent(order::setUser);
        }

        Order savedOrder = orderService.placeOrder(order, cartItems);

        long amount = savedOrder.getTotalAmount().longValue();

        cartService.clearCart();

        if ("momo".equalsIgnoreCase(paymentMethod)) {
            String payUrl = momoPaymentService.createPaymentUrl(
                    savedOrder.getId().toString(),
                    amount,
                    "Thanh toan don hang #" + savedOrder.getId());

            if (payUrl != null) {
                return "redirect:" + payUrl;
            }
        }

        redirectAttributes.addFlashAttribute("earnedPoints", savedOrder.getEarnedPoints());
        redirectAttributes.addFlashAttribute("orderId", savedOrder.getId());
        
        return "redirect:/cart/order-success";
    }

    @GetMapping("/order-success")
    public String orderSuccess(Model model) {
        return "cart/order-success";
    }
}