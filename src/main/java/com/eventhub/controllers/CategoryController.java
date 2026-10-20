package com.eventhub.controllers;

import com.eventhub.common.ApiResponse;
import com.eventhub.dto.request.CreateCategoryRequest;
import com.eventhub.dto.request.UpdateCategoryRequest;
import com.eventhub.dto.response.CategoryResponse;
import com.eventhub.services.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "APIs for managing event categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Get all active categories", description = "Public endpoint to retrieve all active categories")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllActiveCategories() {
        List<CategoryResponse> categories = categoryService.getAllActiveCategories();
        return ResponseEntity.ok(ApiResponse.success("Active categories retrieved successfully", categories));
    }

    @Operation(summary = "Get category by ID", description = "Public endpoint to retrieve category details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", category));
    }

    @Operation(summary = "Get category by slug", description = "Public endpoint to retrieve category details by slug")
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryBySlug(@PathVariable String slug) {
        CategoryResponse category = categoryService.getCategoryBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", category));
    }

    @Operation(summary = "Create new category", description = "Only ADMIN or ORGANIZER can create a new category")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse category = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", category));
    }

    @Operation(summary = "Update category", description = "Only ADMIN can update category details")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        CategoryResponse category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", category));
    }

    @Operation(summary = "Delete category", description = "Only ADMIN can delete a category")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", null));
    }
}
