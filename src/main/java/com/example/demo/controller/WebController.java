package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @Autowired
    private com.example.demo.service.ProductService productService;

    @Autowired
    private com.example.demo.service.CategoryService categoryService;

    @GetMapping({"/", "/index", "/home"})
    public String index(Model model){
        model.addAttribute("appName", "Inner Peace");
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("promoProducts", productService.getPromotionProducts());
        return "index";
    }

    @GetMapping("/403")
    public String accessDenied() {
        return "403";
    }

}
