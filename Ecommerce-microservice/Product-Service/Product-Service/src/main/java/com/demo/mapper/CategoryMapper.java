package com.demo.mapper;

import com.demo.dto.CategoryDto;
import com.demo.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryDto toDto(Category category){
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }

    public Category toEntity(CategoryDto dto){

        return Category.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
    }
}
