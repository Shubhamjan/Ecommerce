package com.demo.service.impl;

import com.demo.dto.SubCategoryDto;
import com.demo.entity.Category;
import com.demo.entity.SubCategory;
import com.demo.repository.CategoryRepository;
import com.demo.repository.SubCategoryRepository;
import com.demo.service.SubCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubCategoryServiceImpl implements SubCategoryService {

    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;

    @Override
    public SubCategoryDto createCategory(SubCategoryDto categoryDto) {

        SubCategory subCategory = new SubCategory();
        subCategory.setName(categoryDto.getName());
        Category category = categoryRepository.findById(categoryDto.getCategoryId()).
                orElseThrow(() -> new RuntimeException("No Category found"));

        subCategory.setCategory(category);

        SubCategory saved = subCategoryRepository.save(subCategory);

        SubCategoryDto dto = new SubCategoryDto();
        dto.setCategoryId(saved.getCategory().getId());
        dto.setId(saved.getId());
        dto.setName(saved.getName());
        return dto;
    }

    @Override
    public List<SubCategoryDto> getAllSUbCategories() {

        List<SubCategory> subCategoryList = subCategoryRepository.findAll();
        List<SubCategoryDto> subCategoryDtoList = new ArrayList<>();
        if (subCategoryList != null && !subCategoryList.isEmpty()) {
            for (SubCategory subCategory : subCategoryList) {
                SubCategoryDto subCategoryDto = new SubCategoryDto();
                subCategoryDto.setId(subCategory.getId());
                subCategoryDto.setName(subCategory.getName());
                subCategoryDto.setCategoryId(subCategory.getCategory().getId());
                subCategoryDtoList.add(subCategoryDto);
            }
        }
        return subCategoryDtoList;
    }

    @Override
    public SubCategoryDto getSubCategoryById(Long id) {

        SubCategory subCategory = subCategoryRepository.findById(id).orElseThrow(()->new RuntimeException("No sub category found"));

        SubCategoryDto subCategoryDto = new SubCategoryDto();
        subCategoryDto.setId(subCategory.getId());
        subCategoryDto.setName(subCategory.getName());
        subCategoryDto.setCategoryId(subCategory.getCategory().getId());
        return subCategoryDto;
    }

    @Override
    public SubCategoryDto updateCategory(Long id, SubCategoryDto dto) {

        SubCategory subCategory = subCategoryRepository.findById(id).orElseThrow(()->new RuntimeException("No subcategory found"));

        subCategory.setName(dto.getName());
        SubCategory update = subCategoryRepository.save(subCategory);

        SubCategoryDto subCategoryDto = new SubCategoryDto();
        subCategoryDto.setName(update.getName());
        subCategoryDto.setId(update.getId());
        subCategoryDto.setCategoryId(update.getCategory().getId());

        return subCategoryDto;
    }

    @Override
    public void deleteCategory(Long id) {
        subCategoryRepository.deleteById(id);
    }
}
