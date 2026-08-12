package com.demo.controller;

import com.demo.dto.CategoryDto;
import com.demo.dto.SubCategoryDto;
import com.demo.service.CategoryService;
import com.demo.service.SubCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product/subCategories")
@RequiredArgsConstructor
public class SubCategoryController {

    private final SubCategoryService subCategoryService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<SubCategoryDto> createSubCategory(@RequestBody SubCategoryDto dto){
        return ResponseEntity.ok(subCategoryService.createCategory(dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<SubCategoryDto>>getAllSubCategory(){
        return ResponseEntity.ok(subCategoryService.getAllSUbCategories());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<SubCategoryDto> getSubCategoryById(@PathVariable Long id){
        return ResponseEntity.ok(subCategoryService.getSubCategoryById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<SubCategoryDto> updateCategory(@PathVariable Long id,@RequestBody SubCategoryDto dto){

        return ResponseEntity.ok(subCategoryService.updateCategory(id,dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id ){
        subCategoryService.deleteCategory(id);
        return ResponseEntity.ok("Category Deleted");
    }
}
