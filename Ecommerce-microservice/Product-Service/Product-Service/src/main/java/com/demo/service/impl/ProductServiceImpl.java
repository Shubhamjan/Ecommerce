package com.demo.service.impl;

import com.demo.ProductServiceApplication;
import com.demo.dto.ProductDto;
import com.demo.entity.Category;
import com.demo.entity.Product;
import com.demo.entity.SubCategory;
import com.demo.event.ProductCreatedEvent;
import com.demo.event.StockUpdateEvent;

import com.demo.mapper.ProductMapper;
import com.demo.repository.CategoryRepository;
import com.demo.repository.ProductRepository;
import com.demo.repository.SubCategoryRepository;
import com.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.SortDirection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;
    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    private final SubCategoryRepository subCategoryRepository;

    private final ProductMapper productMapper;

    private final String uploadDir = System.getProperty("user.dir")+"/upload/products/";

    private static final String TOPIC = "product-event";
    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;

    @Override
    public ProductDto createProduct(ProductDto dto) {

        SubCategory subCategory = null;
        if(dto.getSubCategoryId()!=null){
            subCategory = subCategoryRepository.findById(dto.getSubCategoryId()).orElseThrow(()->new RuntimeException("No sub category found"));


        }
        Product product = productMapper.toEntity(dto,subCategory);

        Product saved = productRepository.save(product);

        ProductCreatedEvent event = new ProductCreatedEvent(saved.getId(),saved.getQuantity());
//
//        kafkaTemplate.send("product-created-topic",event);
        kafkaTemplate.send(TOPIC, event.getProductId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("❌ Failed to publish event for productId={}",
                                event.getProductId(), ex);
                    } else {
                        log.info("✅ Event published | productId={} | partition={} | offset={}",
                                event.getProductId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });

        return productMapper.toDto(saved);
    }

    @Override
    public ProductDto updateProduct(Long id, ProductDto dto) {

        Product product = productRepository.findById(id).orElseThrow(()->new RuntimeException("Product is not found"));
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setDiscountedPrice(dto.getDiscountedPrice());
        if(dto.getSubCategoryId()!=null){
            SubCategory subCategory = new SubCategory();
            subCategory.setId(dto.getSubCategoryId());
            product.setSubCategory(subCategory);
        }
        return productMapper.toDto(productRepository.save(product));
    }

    @Override
    public void deleteProduct(Long id) {

        productRepository.deleteById(id);
    }

    @Override
    public ProductDto getProductById(Long id) {
        Product p = productRepository.findById(id).orElseThrow(()->new RuntimeException("Product not found"));
        log.info("The received product :- "+p.toString());
        return productMapper.toDto(p);
    }

    @Override
//    @Cacheable(value = "products")
    public Page<ProductDto> getAllProduct(int page, int size, String sortBy, String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")?Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page,size,sort);
        Page<Product> productPage = productRepository.findAll(pageable);

        return productPage.map(productMapper::toDto);
    }

    @Override
    public Page<ProductDto> searchProduct(String keyword, int page, int size) {

        Pageable pageable = PageRequest.of(page,size);
        Page<Product> productPage = productRepository.searchProducts(keyword,pageable);

        return productPage.map(productMapper::toDto);
    }

    @Override
    public Page<ProductDto> filterProducts(Long categoryId, double minPrice, double maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page,size);
        Page<Product>productPage = productRepository.advanceFilter(null,categoryId,minPrice,maxPrice,pageable);
        return productPage.map(productMapper::toDto);
    }

    @Override
    public Page<ProductDto> advanceFilter(String keyword, Long subCategoryId, double minPrice, double maxPrice, int page, int size, String sortBy, String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("asc")?Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page,size,sort);

        Page<Product>productPage = productRepository.advanceFilter(keyword,subCategoryId,minPrice,maxPrice,pageable);
        return productPage.map(productMapper::toDto);
    }

    @Override
    public ProductDto uploadImage(Long productId, MultipartFile file) throws IOException {

        Product product = productRepository.findById(productId).orElseThrow(()->new RuntimeException("Product not found"));
        if(file.isEmpty()){
            throw new RuntimeException("Image file is empty");
        }

        long maxSize = 2*1024*1024;

        if(file.getSize()>maxSize){
            throw new RuntimeException("File size must be less than 2MB");
        }

        List<String> allowedType = List.of("image/jpeg","image/png","image/jpg");
        if(!allowedType.contains(file.getContentType())){
            throw new RuntimeException("Only jpeg,png and jpg are allowed");
        }

        String originalName = file.getOriginalFilename();
        if(originalName==null|| !originalName.contains(".")){
            throw new RuntimeException("Invalid file name");
        }

        String ext = originalName.substring(originalName.lastIndexOf(".")+1).toLowerCase();

        List<String>allowedExt = List.of("jpg","png","jpeg");
        if(!allowedExt.contains(ext)){
            throw new RuntimeException("Invalid image extension");
        }

        File folder = new File(uploadDir);
        if(!folder.exists()){
            folder.mkdirs();
        }

        String fileName = UUID.randomUUID().toString()+"."+ext;

        Path filePath  = Paths.get(uploadDir+fileName);

        Files.write(filePath,file.getBytes());
        String imageUrl = "/products/images/"+fileName;

        product.setImageUrl(imageUrl);
        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
    }

    @Override
    public void UpdateProductQuantity(StockUpdateEvent event) {

        Product product = productRepository.findById(event.getProductId()).orElseThrow(()->new RuntimeException("No product found"));

        product.setQuantity(event.getQuantity());
         productRepository.save(product);

    }

    @Override
    public ProductDto uploadToS3(Long id, MultipartFile file) {

        Product product = productRepository.findById(id).orElseThrow(()->new RuntimeException("No product for this id"));

        SubCategory subCategory = subCategoryRepository.findById(product.getSubCategory().getId()).orElseThrow(()->new RuntimeException("No subCategory for this product"));
        String subcategoryName = subCategory.getName();

        try{
            String originalFileName = file.getOriginalFilename();
            String extension = originalFileName!=null && originalFileName.contains(".")
                    ?originalFileName.substring(originalFileName.lastIndexOf(".")):"";

            // e.g. products/electronics/laptops/PROD123/<uuid>.jpg
            String fileName = UUID.randomUUID() + extension;
            String key = String.format("products/%s/%s/%s",subcategoryName,id,fileName);

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(),file.getSize()));
            String url =String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);

            product.setImageUrl(url);
            Product save = productRepository.save(product);

            return productMapper.toDto(
                    save
            );
        }catch (IOException e){
            throw new RuntimeException("Failed to upload image to s3",e);
        }

    }
}
