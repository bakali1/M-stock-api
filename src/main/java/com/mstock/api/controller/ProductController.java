package com.mstock.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mstock.api.payload.Request.ProductRequest;
import com.mstock.api.payload.Responde.GeneralResponde;
import com.mstock.api.services.ProductService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;




@RestController
@RequestMapping("/api/v0/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<GeneralResponde<?>> createProduct(@RequestBody ProductRequest request) {
        GeneralResponde<?> response = productService.createProduct(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    

    @GetMapping
    public ResponseEntity<GeneralResponde<?>> getAllProducts() {
        GeneralResponde<?> response = productService.getAllProducts();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponde<?>> getProduct(@PathVariable Long id) {
        GeneralResponde<?> response = productService.getProduct(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    @PutMapping
    public ResponseEntity<GeneralResponde<?>> updateProduct(@RequestBody ProductRequest productRequest) {
        GeneralResponde<?> response = productService.updateProduct(productRequest);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponde<?>> deleteProduct(@PathVariable Long id) {
        GeneralResponde<?> response = productService.deleteProduct(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
}
