package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.Product;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductService;



@Controller
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    @GetMapping
    public String showProductList(@RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "q", required = false) String keyword,
            Model model) {
        List<Product> products;

        if (categoryId != null) {
            products = productService.getProductsByCategory(categoryId);
            categoryService.getCategoryById(categoryId).ifPresent(cat -> {
                model.addAttribute("activeCategoryId", cat.getId());
                model.addAttribute("activeCategoryName", cat.getName());
            });
        } else if (StringUtils.hasText(keyword)) {
            products = productService.searchProducts(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            products = productService.getAllProducts();
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "products/product-list";
    }

    @GetMapping("/{id}")
    public String showProductDetail(@PathVariable("id") Long id, Model model) {
        return productService.getProductById(id).map(product -> {
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryService.getAllCategories());
            // Optionally add related products
            List<Product> relatedProducts = productService.getProductsByCategory(product.getCategory().getId());
            model.addAttribute("relatedProducts", relatedProducts.stream().filter(p -> !p.getId().equals(id)).limit(5).toList());
            return "products/product-detail";
        }).orElse("redirect:/products");
    }
}
