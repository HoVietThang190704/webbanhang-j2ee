package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Category; 
@Repository 
public interface CategoryRepository extends JpaRepository<Category, Long> { 
	java.util.List<Category> findByParentId(Long parentId);
	java.util.List<Category> findByParentIsNull();
} 
