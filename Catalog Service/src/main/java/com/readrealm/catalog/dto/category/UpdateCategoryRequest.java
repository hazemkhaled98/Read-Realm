package com.readrealm.catalog.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record UpdateCategoryRequest(

        @NotBlank(message = "Id is required")
        @Pattern(regexp = "^[0-9]+$", message = "Id must contain only numbers")
        String id,


        @NotNull
        CategoryRequest details
) {
}
