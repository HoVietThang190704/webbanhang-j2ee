package com.example.demo.controller;

import com.example.demo.model.Order;
import com.example.demo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/momo")
public class MoMoController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/return")
    public String returnUrl(
            @RequestParam("partnerCode") String partnerCode,
            @RequestParam("orderId") String orderId,
            @RequestParam("requestId") String requestId,
            @RequestParam("amount") String amount,
            @RequestParam("orderInfo") String orderInfo,
            @RequestParam("orderType") String orderType,
            @RequestParam("transId") String transId,
            @RequestParam("resultCode") String resultCode,
            @RequestParam("message") String message,
            @RequestParam("payType") String payType,
            @RequestParam("responseTime") String responseTime,
            @RequestParam("extraData") String extraData,
            @RequestParam("signature") String signature,
            Model model) {

        Optional<Order> orderOpt = orderService.getOrderById(Long.parseLong(orderId));
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            if ("0".equals(resultCode)) {
                order.setPaymentStatus("PAID");
                order.setTransactionId(transId);
                model.addAttribute("message", "Thanh toán thành công! Mã giao dịch: " + transId);
            } else {
                order.setPaymentStatus("FAILED");
                model.addAttribute("error", "Thanh toán thất bại! Lỗi: " + message);
            }
            orderService.saveOrder(order);
        } else {
            model.addAttribute("error", "Không tìm thấy đơn hàng.");
        }

        return "cart/order-success";
    }

    @PostMapping("/notify")
    public void notifyUrl(
            @RequestParam("partnerCode") String partnerCode,
            @RequestParam("orderId") String orderId,
            @RequestParam("requestId") String requestId,
            @RequestParam("amount") String amount,
            @RequestParam("orderInfo") String orderInfo,
            @RequestParam("orderType") String orderType,
            @RequestParam("transId") String transId,
            @RequestParam("resultCode") String resultCode,
            @RequestParam("message") String message,
            @RequestParam("payType") String payType,
            @RequestParam("responseTime") String responseTime,
            @RequestParam("extraData") String extraData,
            @RequestParam("signature") String signature) {

        Optional<Order> orderOpt = orderService.getOrderById(Long.parseLong(orderId));
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            if ("0".equals(resultCode)) {
                order.setPaymentStatus("PAID");
                order.setTransactionId(transId);
            } else {
                order.setPaymentStatus("FAILED");
            }
            orderService.saveOrder(order);
        }
    }
}
