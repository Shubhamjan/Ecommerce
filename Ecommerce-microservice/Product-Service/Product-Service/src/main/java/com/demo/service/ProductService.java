package com.demo.service;

import com.demo.dto.ProductDto;
import com.demo.entity.Product;
import com.demo.event.StockUpdateEvent;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductService {

    ProductDto createProduct(ProductDto dto);

    ProductDto updateProduct(Long id, ProductDto dto);

    void deleteProduct(Long id);

    ProductDto getProductById(Long id);

    Page<ProductDto> getAllProduct(int page, int size, String sortBy, String direction);

    Page<ProductDto> searchProduct(String keyword, int page, int size);

    Page<ProductDto> filterProducts(Long categoryId, double minPrice, double maxPrice, int page, int size);

    Page<ProductDto> advanceFilter(String keyword, Long subCategoryId, double minPrice, double maxPrice, int page, int size, String sortBy, String sortDirection);

    ProductDto uploadImage(Long productId, MultipartFile file) throws IOException;

    void UpdateProductQuantity(StockUpdateEvent event);

    ProductDto uploadToS3(Long id, MultipartFile file);
}
