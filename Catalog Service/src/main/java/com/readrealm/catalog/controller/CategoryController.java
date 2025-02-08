package com.readrealm.catalog.controller;


import com.readrealm.catalog.dto.category.CategoryRequest;
import com.readrealm.catalog.dto.category.CategoryResponse;
import com.readrealm.catalog.dto.category.UpdateCategoryRequest;
import com.readrealm.catalog.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/categories")
@RequiredArgsConstructor

public class CategoryController {


    private final CategoryService categoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryResponse> getAllCategories() {

        return categoryService.findAllCategories();
    }

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryResponse getCategory(@PathVariable long id) {
        return categoryService.findCategoryById(id);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String addCategory(@Valid @RequestBody CategoryRequest categoryRequest) {
        return categoryService.addCategory(categoryRequest);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public String updateCategory(@Valid @RequestBody UpdateCategoryRequest updateCategoryRequest) {
        return categoryService.updateCategory(updateCategoryRequest);
    }


    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable long id) {
        categoryService.deleteCategory(id);
    }
}
