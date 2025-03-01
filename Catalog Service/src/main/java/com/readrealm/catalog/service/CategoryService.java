package com.readrealm.catalog.service;

import com.readrealm.catalog.dto.category.CategoryRequest;
import com.readrealm.catalog.dto.category.CategoryResponse;
import com.readrealm.catalog.dto.category.UpdateCategoryRequest;
import com.readrealm.catalog.entity.Category;
import com.readrealm.catalog.mapper.CategoryMapper;
import com.readrealm.catalog.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(isolation = Isolation.REPEATABLE_READ)
public class CategoryService {
    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "categories", cacheManager = "cacheManager")
    public List<CategoryResponse> findAllCategories() {
        log.info("Finding All Categories");
        return categoryRepository
                .findAllCategoriesDetails()
                .stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "categoryById", key = "#id", cacheManager = "cacheManager")
    public CategoryResponse findCategoryById(Long id) {
        log.info("Finding Category by ID {}", id);
        return categoryRepository.findCategoryDetailsById(id)
                .map(categoryMapper::toCategoryResponse)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Category with id %s not found", id)));
    }

    @Transactional
    @CachePut(cacheNames = "categoryById", key = "#createCategory.id()", cacheManager = "cacheManager")
    @CacheEvict(cacheNames = "categories", allEntries = true, cacheManager = "cacheManager")
    public CategoryResponse addCategory(CategoryRequest request) {
        log.info("Adding Category {}", request);
        Category createCategory = Category
                .builder()
                .name(request.name())
                .build();
        createCategory = categoryRepository.save(createCategory);
        return categoryMapper.toCategoryResponse(createCategory);
    }

    @Transactional
    @CachePut(cacheNames = "categoryById", key = "#updatedCategory.id()", cacheManager = "cacheManager")
    @CacheEvict(cacheNames = "categories", allEntries = true, cacheManager = "cacheManager")
    public CategoryResponse updateCategory(UpdateCategoryRequest request) {
        log.info("Updating Category {}", request);
        long id = Long.parseLong(request.id());
        Optional<Category> optionalCategory = categoryRepository.findById(id);
        Category updatedCategory = optionalCategory
                .orElseThrow(() -> new EntityNotFoundException(String.format("Category with id %s not found", request.id())));
        updatedCategory.setName(request.details().name());
        updatedCategory = categoryRepository.save(updatedCategory);
        return categoryMapper.toCategoryResponse(updatedCategory);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "categoryById", key = "#isbn", cacheManager = "cacheManager"),
            @CacheEvict(cacheNames = "categories", allEntries = true, cacheManager = "cacheManager")
    })
    public void deleteCategory(long id) {
        log.info("Deleting category with id {}", id);
        categoryRepository.deleteById(id);
    }
}
