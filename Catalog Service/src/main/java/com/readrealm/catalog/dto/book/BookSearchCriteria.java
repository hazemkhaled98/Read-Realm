package com.readrealm.catalog.dto.book;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record BookSearchCriteria(
        @Size(max = 255, message = "Title search term cannot exceed 255 characters")
        String title,

        @Size(max = 255, message = "Author name search term cannot exceed 255 characters")
        @Pattern(regexp = "^[a-zA-Z\\s.-]*$", message = "Author name can only contain letters, spaces, dots, and hyphens")
        String authorName,

        @Size(max = 100, message = "Category search term cannot exceed 100 characters")
        @Pattern(regexp = "^[a-zA-Z\\s&-]*$", message = "Category can only contain letters, spaces, ampersands, and hyphens")
        String category,

        @Min(value = 1, message = "Page number cannot be lower than 1")
        Integer pageNumber,

        @Min(value = 1, message = "Page size must be at least 1")
        @Max(value = 100, message = "Page size cannot exceed 100")
        Integer pageSize,

        @Pattern(regexp = "^(?i)(title|price)$", message = "Sort by must be title or price")
        String sortBy,

        @Pattern(regexp = "^(?i)(asc|desc)$", message = "Sort order must be either asc or desc")
        String sortOrder
) {}
