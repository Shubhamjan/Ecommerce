package com.demo.service.impl;

import com.demo.dto.CategoryDto;
import com.demo.entity.Category;
import com.demo.mapper.CategoryMapper;
import com.demo.repository.CategoryRepository;
import com.demo.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {

        Category category = categoryMapper.toEntity(categoryDto);

        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public CategoryDto updateCategory(Long id, CategoryDto dto) {

        Category category = categoryRepository.findById(id).orElseThrow(()->new RuntimeException("No Category found"));
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public CategoryDto getCategory(Long id) {

        return categoryRepository.findById(id).map(categoryMapper::toDto).orElseThrow(()->new RuntimeException("No category found"));
    }

    @Override
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream().map(categoryMapper::toDto).toList();
    }
}
