package com.ogidazepam.e_commerce.service;

import com.ogidazepam.e_commerce.dto.ProductViewDTO;
import com.ogidazepam.e_commerce.model.Product;
import com.ogidazepam.e_commerce.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductViewDTO> findAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(p -> new ProductViewDTO(
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getQuantity()
        )).collect(Collectors.toList());
    }

    public ProductViewDTO findProduct(long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        return new ProductViewDTO(
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void saveProduct(@Valid ProductViewDTO dto) {
        Product product = new Product(
                dto.name(),
                dto.description(),
                dto.price(),
                dto.quantity()
        );

        productRepository.save(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void updateProduct(long id, @Valid ProductViewDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setPrice(dto.price());
        product.setQuantity(dto.quantity());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(long id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        productRepository.delete(product);
    }

    public List<ProductViewDTO> findProductsByName(String name) {
        List<Product> products = productRepository.findAllByNameContainingIgnoreCase(name);
        return products.stream().map(p -> new ProductViewDTO(
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getQuantity()
        )).collect(Collectors.toList());
    }
}
