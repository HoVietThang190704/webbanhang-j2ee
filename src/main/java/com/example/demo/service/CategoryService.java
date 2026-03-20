package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Category;
import com.example.demo.repository.CategoryRepository;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

/**
 * Service class for managing categories.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Retrieve all categories from the database.
     * 
     * @return a list of categories
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> getTopLevelCategories() {
        return categoryRepository.findByParentIsNull();
    }

    /**
     * Retrieve a category by its id.
     * 
     * @param id the id of the category to retrieve
     * @return an Optional containing the found category or empty if not found
     */
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    /**
     * Add a new category to the database.
     * 
     * @param category the category to add
     */
    public void addCategory(Category category) {
        if (category.getSlug() == null || category.getSlug().trim().isEmpty()) {
            category.setSlug(toSlug(category.getName()));
        }
        // Resolve parent if only id provided
        if (category.getParent() != null && category.getParent().getId() != null) {
            Category parent = categoryRepository.findById(category.getParent().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid parent id: " + category.getParent().getId()));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
        categoryRepository.save(category);
    }

    /**
     * Update an existing category.
     * 
     * @param category the category with updated information
     */
    public void updateCategory(@NotNull Category category) {
        Category existingCategory = categoryRepository.findById(category.getId())
                .orElseThrow(() -> new IllegalStateException("Category with ID " +
                        category.getId() + " does not exist."));
        existingCategory.setName(category.getName());
        if (category.getSlug() == null || category.getSlug().trim().isEmpty()) {
            existingCategory.setSlug(toSlug(category.getName()));
        } else {
            existingCategory.setSlug(category.getSlug());
        }
        // Resolve parent reference explicitly
        if (category.getParent() != null && category.getParent().getId() != null) {
            Category parent = categoryRepository.findById(category.getParent().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid parent id: " + category.getParent().getId()));
            existingCategory.setParent(parent);
        } else {
            existingCategory.setParent(null);
        }
        categoryRepository.save(existingCategory);
    }

    public void deleteCategoryById(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new IllegalStateException("Category with ID " + id + " does not exist.");
        }
        // Before deleting, detach children (make them top-level)
        java.util.List<Category> children = categoryRepository.findByParentId(id);
        if (children != null && !children.isEmpty()) {
            for (Category c : children) {
                c.setParent(null);
                categoryRepository.save(c);
            }
        }
        categoryRepository.deleteById(id);
    }

    // Utility: convert name to slug-ish string
    private String toSlug(String input) {
        if (input == null) return null;
        String normalized = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD);
        String slug = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        slug = slug.toLowerCase().replaceAll("[^a-z0-9\\s-]", "");
        slug = slug.replaceAll("\\s+", "-").replaceAll("-{2,}", "-");
        slug = slug.replaceAll("^-|-$", "");
        return slug;
    }
}