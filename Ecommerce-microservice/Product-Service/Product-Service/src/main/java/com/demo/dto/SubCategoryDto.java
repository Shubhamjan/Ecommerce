package com.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubCategoryDto {

    private Long id;

    @NotBlank(message = "SubCategory name cannot be empty")
    private String name;

    @NotNull(message = "Category id is required")
    private Long categoryId;

    // Optional: include products in response
    private List<ProductDto> products;
}