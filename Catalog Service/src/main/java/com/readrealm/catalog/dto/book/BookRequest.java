package com.readrealm.catalog.dto.book;

import jakarta.validation.constraints.*;
import lombok.Builder;
import org.hibernate.validator.constraints.ISBN;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record BookRequest(

        @NotBlank(message = "ISBN cannot be blank")
        @ISBN(message = "ISBN is invalid")
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
