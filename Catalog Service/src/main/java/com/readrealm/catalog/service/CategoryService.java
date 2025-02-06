package com.readrealm.catalog.service;


import com.readrealm.catalog.dto.category.CategoryRequest;
import com.readrealm.catalog.dto.category.CategoryResponse;
import com.readrealm.catalog.dto.category.UpdateCategoryRequest;
import com.readrealm.catalog.entity.Category;
import com.readrealm.catalog.exception.NotFoundException;
import com.readrealm.catalog.mapper.CategoryMapper;
import com.readrealm.catalog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public List<CategoryResponse> findAllCategories() {

        log.info("Finding All Categories");
        return categoryRepository
                .findAllCategoriesDetails()
                .stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse findCategoryById(Long id) {
        log.info("Finding Category by ID {}", id);

        return categoryRepository.findCategoryDetailsById(id)
                .map(categoryMapper::toCategoryResponse)
                .orElseThrow(() -> new NotFoundException(String.format("Category with id %s not found", id)));
    }

    public String addCategory(CategoryRequest request) {

        log.info("Adding Category {}", request);
        Category createCategory = Category
                .builder()
                .name(request.name())
                .build();

        createCategory = categoryRepository.save(createCategory);

        return String.format("Author with id %s added", createCategory.getId());
    }

    public String updateCategory(UpdateCategoryRequest request) {
        log.info("Updating Category {}", request);

        long id = Long.parseLong(request.id());

        Optional<Category> optionalCategory = categoryRepository.findById(id);

        Category updatedCategory = optionalCategory.orElseThrow(() -> new NotFoundException(String.format("Category with id %s not found", request.id())));

        updatedCategory.setName(request.details().name());

        updatedCategory = categoryRepository.save(updatedCategory);

        return String.format("Category with id %s updated", updatedCategory.getId());
    }


    public void deleteCategory(long id) {
        log.info("Deleting category with id {}", id);
        categoryRepository.deleteById(id);
    }
}
