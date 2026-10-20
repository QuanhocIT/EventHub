package com.eventhub.services;

import com.eventhub.dto.request.CreateCategoryRequest;
import com.eventhub.dto.request.UpdateCategoryRequest;
import com.eventhub.dto.response.CategoryResponse;
import com.eventhub.entities.Category;
import com.eventhub.exceptions.BadRequestException;
import com.eventhub.exceptions.ResourceNotFoundException;
import com.eventhub.repositories.CategoryRepository;
import com.eventhub.services.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category sampleCategory;

    @BeforeEach
    void setUp() {
        sampleCategory = Category.builder()
                .id(1L)
                .name("Technology & Conference")
                .slug("technology-conference")
                .description("Tech conferences and hackathons")
                .iconUrl("https://example.com/tech.png")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should return all active categories successfully")
    void getAllActiveCategories_ReturnsList() {
        when(categoryRepository.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(sampleCategory));

        List<CategoryResponse> result = categoryService.getAllActiveCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Technology & Conference");
        assertThat(result.get(0).getSlug()).isEqualTo("technology-conference");
    }

    @Test
    @DisplayName("Should get category by ID successfully")
    void getCategoryById_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));

        CategoryResponse result = categoryService.getCategoryById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Technology & Conference");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category not found")
    void getCategoryById_NotFound_ThrowsException() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category");
    }

    @Test
    @DisplayName("Should create category successfully")
    void createCategory_Success() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Music & Festival")
                .description("Music shows")
                .iconUrl("https://example.com/music.png")
                .build();

        when(categoryRepository.existsByNameIgnoreCase("Music & Festival")).thenReturn(false);
        when(categoryRepository.existsBySlugIgnoreCase("music-festival")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category c = invocation.getArgument(0);
            c.setId(2L);
            return c;
        });

        CategoryResponse response = categoryService.createCategory(request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Music & Festival");
        assertThat(response.getSlug()).isEqualTo("music-festival");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when category name already exists")
    void createCategory_DuplicateName_ThrowsException() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Technology & Conference")
                .build();

        when(categoryRepository.existsByNameIgnoreCase("Technology & Conference")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should update category successfully")
    void updateCategory_Success() {
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("Technology & AI Summit")
                .description("Updated description")
                .active(true)
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
        when(categoryRepository.existsByNameIgnoreCase("Technology & AI Summit")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response = categoryService.updateCategory(1L, request);

        assertThat(response.getName()).isEqualTo("Technology & AI Summit");
        verify(categoryRepository).save(sampleCategory);
    }

    @Test
    @DisplayName("Should delete category successfully")
    void deleteCategory_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));

        categoryService.deleteCategory(1L);

        verify(categoryRepository).delete(sampleCategory);
    }
}
