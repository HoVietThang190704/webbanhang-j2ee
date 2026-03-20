package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import com.example.demo.model.Product;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductService;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String showAdminProductList(@RequestParam(value = "categoryId", required = false) Long categoryId,
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
        return "admin/products/product-list";
    }

    // For adding a new product
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/products/add-product";
    }

    // Process the form for adding a new product
    @PostMapping("/add")
    public String addProduct(@jakarta.validation.Valid Product product,
            org.springframework.validation.BindingResult result, 
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/products/add-product";
        }
        if (product.getCategory() != null && product.getCategory().getId() != null) {
            com.example.demo.model.Category cat = categoryService.getCategoryById(product.getCategory().getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Invalid category Id: " + product.getCategory().getId()));
            product.setCategory(cat);
        } else {
            product.setCategory(null);
        }

        // Handle Image Upload
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String originalFilename = imageFile.getOriginalFilename();
                String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
                String uniqueFilename = UUID.randomUUID().toString() + extension;
                Path uploadPath = Paths.get("src/main/resources/static/images/");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Files.copy(imageFile.getInputStream(), uploadPath.resolve(uniqueFilename), StandardCopyOption.REPLACE_EXISTING);
                product.setImage(uniqueFilename);
                product.setImagePath("/images/" + uniqueFilename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        productService.addProduct(product);
        return "redirect:/admin/products";
    }

    // For editing a product
    @GetMapping("/edit/{id}")
    public String showEditForm(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/products/update-product";
    }

    // Process the form for updating a product
    @PostMapping("/update/{id}")
    public String updateProduct(@org.springframework.web.bind.annotation.PathVariable Long id,
            @jakarta.validation.Valid Product product, org.springframework.validation.BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model) {
        if (result.hasErrors()) {
            product.setId(id);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/products/update-product";
        }
        if (product.getCategory() != null && product.getCategory().getId() != null) {
            com.example.demo.model.Category cat = categoryService.getCategoryById(product.getCategory().getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Invalid category Id: " + product.getCategory().getId()));
            product.setCategory(cat);
        } else {
            product.setCategory(null);
        }

        // Handle Image Upload
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String originalFilename = imageFile.getOriginalFilename();
                String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
                String uniqueFilename = UUID.randomUUID().toString() + extension;
                Path uploadPath = Paths.get("src/main/resources/static/images/");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Files.copy(imageFile.getInputStream(), uploadPath.resolve(uniqueFilename), StandardCopyOption.REPLACE_EXISTING);
                product.setImage(uniqueFilename);
                product.setImagePath("/images/" + uniqueFilename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (product.getId() != null) {
            // Keep the old image if no new image is provided
             Product existingProduct = productService.getProductById(product.getId()).orElse(null);
             if (existingProduct != null) {
                 product.setImage(existingProduct.getImage());
                 product.setImagePath(existingProduct.getImagePath());
             }
        }

        productService.updateProduct(product);
        return "redirect:/admin/products";
    }

    // Handle request to delete a product
    @GetMapping("/delete/{id}")
    public String deleteProduct(@org.springframework.web.bind.annotation.PathVariable Long id, 
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProductById(id);
            redirectAttributes.addFlashAttribute("message", "Xóa sản phẩm thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/products";
    }
}