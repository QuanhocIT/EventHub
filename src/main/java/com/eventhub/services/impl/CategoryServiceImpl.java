package com.eventhub.services.impl;

import com.eventhub.dto.request.CreateCategoryRequest;
import com.eventhub.dto.request.UpdateCategoryRequest;
import com.eventhub.dto.response.CategoryResponse;
import com.eventhub.entities.Category;
import com.eventhub.exceptions.BadRequestException;
import com.eventhub.exceptions.ResourceNotFoundException;
import com.eventhub.repositories.CategoryRepository;
import com.eventhub.services.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    @Override
    public List<CategoryResponse> getAllActiveCategories() {
        log.debug("Fetching all active categories");
        return categoryRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        log.debug("Fetching all categories");
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        log.debug("Fetching category by ID: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return mapToResponse(category);
    }

    @Override
    public CategoryResponse getCategoryBySlug(String slug) {
        log.debug("Fetching category by slug: {}", slug);
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));
        return mapToResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        log.info("Creating new category: {}", request.getName());

        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BadRequestException("Category with name '" + request.getName() + "' already exists");
        }

        String slug = toSlug(request.getName());
        if (categoryRepository.existsBySlugIgnoreCase(slug)) {
            slug = slug + "-" + System.currentTimeMillis() % 10000;
        }

        Category category = Category.builder()
                .name(request.getName().trim())
                .slug(slug)
                .description(request.getDescription())
                .iconUrl(request.getIconUrl())
                .active(true)
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}, slug: {}", saved.getId(), saved.getSlug());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        log.info("Updating category ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (!category.getName().equalsIgnoreCase(request.getName().trim())
                && categoryRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new BadRequestException("Another category with name '" + request.getName() + "' already exists");
        }

        category.setName(request.getName().trim());
        category.setSlug(toSlug(request.getName().trim()));
        category.setDescription(request.getDescription());
        category.setIconUrl(request.getIconUrl());
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }

        Category updated = categoryRepository.save(category);
        log.info("Category ID: {} updated successfully", id);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting category ID: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        categoryRepository.delete(category);
        log.info("Category ID: {} deleted", id);
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .iconUrl(category.getIconUrl())
                .active(category.isActive())
                .createdAt(category.getCreatedAt())
                .build();
    }

    private String toSlug(String input) {
        if (input == null) return "";
        String nowhitespace = WHITESPACE.matcher(input.trim()).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        slug = slug.replaceAll("-{2,}", "-");
        slug = slug.replaceAll("^-|-$", "");
        return slug.toLowerCase(Locale.ENGLISH);
    }
}
