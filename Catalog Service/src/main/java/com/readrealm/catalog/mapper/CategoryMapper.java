package com.readrealm.catalog.mapper;

import com.readrealm.catalog.dto.category.CategoryResponse;
import com.readrealm.catalog.entity.Category;
import com.readrealm.catalog.repository.projection.CategoryDetails;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse toCategoryResponse(CategoryDetails categoryDetails);

    CategoryResponse toCategoryResponse(Category category);
}
