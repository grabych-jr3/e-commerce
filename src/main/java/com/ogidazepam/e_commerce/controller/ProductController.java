package com.ogidazepam.e_commerce.controller;

import com.ogidazepam.e_commerce.dto.ProductViewDTO;
import com.ogidazepam.e_commerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/* TODO
    Налаштувати безпеку для методів POST, PATCH, DELETE
    Тільки адмін має право на ці методи
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductViewDTO>> findAllProducts(){
        List<ProductViewDTO> dtoList = productService.findAllProducts();
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductViewDTO> findProduct(@PathVariable long id){
        ProductViewDTO dto = productService.findProduct(id);
        return ResponseEntity.ok(dto);
    }

    // !ADMIN ONLY
    @PostMapping
    public ResponseEntity<Void> createProduct(@RequestBody @Valid ProductViewDTO dto){
        productService.saveProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // !ADMIN ONLY
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateProduct(@PathVariable long id,
                                              @RequestBody @Valid ProductViewDTO dto){
        productService.updateProduct(id, dto);
        return ResponseEntity.ok().build();
    }

    // !ADMIN ONLY
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable long id){
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
