package com.demo.repository;

import com.demo.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product,Long> {

//    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Product> findByPriceBetween(Double min,Double max,Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String keyword,Pageable pageable);

    @Query("select p from Product p "+
    "where lower(p.name) like lower(concat('%',:keyword,'%'))"+
            "or lower(p.description) like lower(concat('%',:keyword,'%'))")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);


    @Query("Select p from Product p "+
    "where (:keyword is null or lower(p.name) like lower(concat('%',:keyword,'%'))) "+
    "and (:subCategory is null or p.subCategory.id = :subCategoryId) "+
    "and (p.price between :minPrice and :maxPrice)")
    Page<Product>advanceFilter(@Param("keyword")String kewyword,
                               @Param("subCategoryId")Long subCategoryId,
                               @Param("minPrice")Double minPrice,
                               @Param("maxPrice")Double maxPrice,
                               Pageable pageable);

}
