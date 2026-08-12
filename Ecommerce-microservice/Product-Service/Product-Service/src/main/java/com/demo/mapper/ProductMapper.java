package com.demo.mapper;

import com.demo.dto.ProductDto;
import com.demo.entity.Category;
import com.demo.entity.Product;
import com.demo.entity.SubCategory;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductDto  toDto(Product product){
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountedPrice(product.getDiscountedPrice())
                .quantity(product.getQuantity())
                .imageUrl(product.getImageUrl())
                .brand(product.getBrand())
                .subCategoryId(product.getSubCategory().getId())
                .build();
    }

    public Product toEntity(ProductDto dto, SubCategory subCategory){
        return Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .discountedPrice(dto.getDiscountedPrice())
                .quantity(dto.getQuantity())
                .brand(dto.getBrand())
                .imageUrl(dto.getImageUrl())
                .subCategory(subCategory)
                .build();
    }



    
}
