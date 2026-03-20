package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.model.Order;
import com.example.demo.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public String listOrders(Model model) {
        if (!model.containsAttribute("order")) {
            model.addAttribute("order", new Order());
        }
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("editMode", false);
        return "orders/list-orders";
    }

    @PostMapping("/save")
    public String saveOrder(@Valid @ModelAttribute("order") Order order,
                            BindingResult result,
                            RedirectAttributes redirectAttrs,
                            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("orders", orderService.getAllOrders());
            model.addAttribute("editMode", order.getId() != null);
            return "orders/list-orders";
        }

        boolean isNew = order.getId() == null;
        orderService.saveOrder(order);
        redirectAttrs.addFlashAttribute("message", isNew ? "Order added successfully!" : "Order updated successfully!");
        return "redirect:/orders";
    }

    @GetMapping("/edit/{id}")
    public String editOrder(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttrs) {
        return orderService.getOrderById(id)
                .map(ord -> {
                    model.addAttribute("order", ord);
                    model.addAttribute("orders", orderService.getAllOrders());
                    model.addAttribute("editMode", true);
                    return "orders/list-orders";
                }).orElseGet(() -> {
                    redirectAttrs.addFlashAttribute("message", "Order not found!");
                    return "redirect:/orders";
                });
    }

    @PostMapping("/delete/{id}")
    public String deleteOrder(@PathVariable("id") Long id, RedirectAttributes redirectAttrs) {
        try {
            orderService.deleteOrderById(id);
            redirectAttrs.addFlashAttribute("message", "Order deleted successfully!");
        } catch (IllegalStateException ex) {
            redirectAttrs.addFlashAttribute("message", ex.getMessage());
        }
        return "redirect:/orders";
    }
}
