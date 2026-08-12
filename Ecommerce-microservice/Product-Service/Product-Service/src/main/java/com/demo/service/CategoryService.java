package com.demo.service;

import com.demo.dto.CategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto createCategory(CategoryDto categoryDto);

    CategoryDto updateCategory(Long id,CategoryDto dto);
    void deleteCategory(Long id);

    CategoryDto getCategory(Long id);
    List<CategoryDto> getAllCategories();
}
