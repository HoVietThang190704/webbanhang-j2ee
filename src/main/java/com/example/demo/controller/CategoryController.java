package com.example.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.Category;
import com.example.demo.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("parents", categoryService.getTopLevelCategories());
        return "admin/categories/add-category";
    }

    @PostMapping("/add")
    public String addCategory(@Valid Category category, BindingResult result,
            @RequestParam(value = "parentId", required = false) Long parentId,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("parents", categoryService.getTopLevelCategories());
            return "admin/categories/add-category";
        }
        if (parentId != null) {
            Category parent = new Category();
            parent.setId(parentId);
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
        categoryService.addCategory(category);
        return "redirect:/admin/categories";
    }

    @GetMapping
    public String listCategories(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("parents", categoryService.getTopLevelCategories());
        return "admin/categories/categories-list";
    }

    // GET request to show category edit form
    @GetMapping("/edit/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category Id:"
                        + id));
        model.addAttribute("category", category);
        model.addAttribute("parents", categoryService.getTopLevelCategories());
        return "admin/categories/update-category";
    }

    // POST request to update category
    @PostMapping("/update/{id}")
    public String updateCategory(@PathVariable("id") Long id, @Valid Category category,
            BindingResult result,
            @RequestParam(value = "parentId", required = false) Long parentId,
            Model model) {
        if (result.hasErrors()) {
            category.setId(id);
            model.addAttribute("parents", categoryService.getTopLevelCategories());
            return "admin/categories/update-category";
        }
        category.setId(id);
        if (parentId != null) {
            Category parent = new Category();
            parent.setId(parentId);
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
        categoryService.updateCategory(category);
        return "redirect:/admin/categories";
    }

    // GET request for deleting category
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id, Model model) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category Id:"
                        + id));
        categoryService.deleteCategoryById(id);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "redirect:/admin/categories";
    }
}
