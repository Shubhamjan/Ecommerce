package com.demo.controller;

import com.demo.dto.ProductDto;
import com.demo.service.ProductService;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto dto){

        return ResponseEntity.ok(productService.createProduct(dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id,@RequestBody ProductDto dto){
        return ResponseEntity.ok(productService.updateProduct(id,dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
        return ResponseEntity.ok("Product deleted");
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id){
        log.info("The request received for "+id);
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
//    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<Page<ProductDto>> getAllProducts(@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size,
                                                           @RequestParam(defaultValue = "id") String sortBy,@RequestParam(defaultValue = "asc")String direction){
        return ResponseEntity.ok(productService.getAllProduct(page,size,sortBy,direction));

    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/search")
    public ResponseEntity<Page<ProductDto>> searchProducts(@RequestParam String keyword,@RequestParam(defaultValue = "0")int page,@RequestParam(defaultValue = "10")int size){

        return ResponseEntity.ok(productService.searchProduct(keyword,page,size));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/filter")
    public ResponseEntity<Page<ProductDto>> filterProducts(@RequestParam(required = false)Long categoryId,@RequestParam(required = false)Double minPrice,@RequestParam(required = false)Double maxPrice,
                                                           @RequestParam(defaultValue = "0")int page,@RequestParam(defaultValue = "10")int size){
        return ResponseEntity.ok(productService.filterProducts(categoryId,minPrice,maxPrice,page,size));
    }

    @PreAuthorize("hasRole('ADMIN','USER')")
    @GetMapping("/advance")
    public ResponseEntity<Page<ProductDto>> advanceSearch(@RequestParam String keyword,@RequestParam(required = false)Long subCategoryId,@RequestParam(required = false)Double minPrice,@RequestParam(required = false)Double maxPrice,
                                                          @RequestParam(defaultValue = "0")int page,@RequestParam(defaultValue = "10")int size,@RequestParam(defaultValue = "id") String sortBy,@RequestParam(defaultValue = "asc")String direction){
        return ResponseEntity.ok(productService.advanceFilter(keyword,subCategoryId,minPrice,maxPrice,page,size,sortBy,direction));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/upload-image")
    public ResponseEntity<ProductDto> uploadImage(@PathVariable Long id, @RequestParam("file")MultipartFile file) throws IOException{

//        return ResponseEntity.ok(productService.uploadImage(id,file));
        return ResponseEntity.ok(productService.uploadToS3(id,file));
    }




}
