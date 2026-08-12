package com.demo.service;

import com.demo.dto.CategoryDto;
import com.demo.dto.SubCategoryDto;

import java.util.List;

public interface SubCategoryService {
    SubCategoryDto createCategory(SubCategoryDto categoryDto);

    List<SubCategoryDto> getAllSUbCategories();

    SubCategoryDto getSubCategoryById(Long id);

    SubCategoryDto updateCategory(Long id, SubCategoryDto dto);

    void deleteCategory(Long id);
}
