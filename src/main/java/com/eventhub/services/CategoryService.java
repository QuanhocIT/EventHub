package com.eventhub.services;

import com.eventhub.dto.request.CreateCategoryRequest;
import com.eventhub.dto.request.UpdateCategoryRequest;
import com.eventhub.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getAllActiveCategories();

    List<CategoryResponse> getAllCategories();

    CategoryResponse getCategoryById(Long id);

    CategoryResponse getCategoryBySlug(String slug);

    CategoryResponse createCategory(CreateCategoryRequest request);

    CategoryResponse updateCategory(Long id, UpdateCategoryRequest request);

    void deleteCategory(Long id);
}
