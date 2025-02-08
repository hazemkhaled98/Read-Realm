package com.readrealm.catalog.dto.book;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record BookRequest(

        @NotBlank(message = "ISBN cannot be blank")
        @Pattern(regexp = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$",
                message = "Invalid ISBN format")
        String isbn,

        @NotBlank(message = "Title cannot be blank")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @NotBlank(message = "Description cannot be blank")
        @Size(max = 2000, message = "Description cannot exceed 2000 characters")
        String description,

        @NotNull(message = "Price must be provided")
        @DecimalMin(value = "0.0", inclusive = false, message = "If provided, price must be greater than 0")
        @Digits(integer = 6, fraction = 2, message = "Price can have up to 6 digits and 2 decimal places")
        BigDecimal price,

        @NotEmpty(message = "Authors IDs list cannot be empty")
        @Size(max = 10, message = "Cannot have more than 10 authors")
        List<@Positive(message = "Author ID must be positive") Long> authorsIds,

        @NotEmpty(message = "Categories IDs list cannot be empty")
        @Size(max = 5, message = "Cannot have more than 5 categories")
        List<@Positive(message = "Category ID must be positive") Long> categoriesIds
) {}
