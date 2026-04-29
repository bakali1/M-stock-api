package com.mstock.api.services;

import java.util.List;

import com.mstock.api.DTO.ProductDTO;
import com.mstock.api.payload.Request.ProductRequest;
import com.mstock.api.payload.Responde.GeneralResponde;

public interface ProductService {
    GeneralResponde<?> createProduct(ProductRequest productRequest);
    GeneralResponde<List<ProductDTO>> getAllProducts();
	GeneralResponde<?> getProduct(Long id);
    GeneralResponde<?> updateProduct(ProductRequest productRequest);
    GeneralResponde<?> deleteProduct(Long id);
}
