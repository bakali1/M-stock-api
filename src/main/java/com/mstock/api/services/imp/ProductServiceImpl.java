package com.mstock.api.services.imp;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.mstock.api.DTO.ProductDTO;
import com.mstock.api.Mappers.ProductMapper;
import com.mstock.api.entities.Product;
import com.mstock.api.payload.Request.ProductRequest;
import com.mstock.api.payload.Responde.GeneralResponde;
import com.mstock.api.repositories.ProductRepository;
import com.mstock.api.services.ProductService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService{

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public GeneralResponde<?> createProduct(ProductRequest productRequest) {

        if (productRepository.existsByNsnCode(productRequest.getNsnCode())) {
            return GeneralResponde.<Void>builder()
                    .status(HttpStatus.CONFLICT.value())
                    .msg("Product with this email already exists")
                    .build();
        }

        Product product = new Product();
        product.updateFromRequest(productRequest);

        ProductDTO savedProduct = productMapper.toProductDTO(productRepository.save(product));

        return GeneralResponde.<ProductDTO>builder()
                .status(HttpStatus.CREATED.value())
                .msg("Product created successfully")
                .data(savedProduct)
                .build();
    }

    @Override
    public GeneralResponde<List<ProductDTO>> getAllProducts() {
        return GeneralResponde.<List<ProductDTO>>builder()
                                .status(200)
                                .data(productMapper.toProductDTO(productRepository.findByActiveTrue()))
                                .msg("All Product").build();
    }

	@Override
	public GeneralResponde<?> getProduct(Long id) {
		Product product = productRepository.findByIdAndActiveTrueWithoutRelations(id).orElse(null);
        if(product == null){
            return GeneralResponde.<Void>builder()
            .status(HttpStatus.NOT_FOUND.value())
            .data(null).msg("Product not found").build();
        }
        return GeneralResponde.<ProductDTO>builder().data(productMapper.toProductDTO(product)).status(HttpStatus.OK.value()).msg("Product was found").build();
	}
    
    @Override
    public GeneralResponde<?> updateProduct(ProductRequest request){
        Product product = productRepository.findByIdWithoutRelations(request.getId()).orElse(null);
        if(product == null){
            return GeneralResponde.<Void>builder()
            .status(HttpStatus.NOT_FOUND.value())
            .data(null).msg("Product not found").build();
        }
        
        // Use the Product's updateFromRequest method for partial updates
        product.updateFromRequest(request);
        
        return GeneralResponde.<ProductDTO>builder().data(productMapper.toProductDTO(productRepository.save(product))).msg("Product updated successfully").status(HttpStatus.OK.value()).build();
    }

    public GeneralResponde<?> deleteProduct(Long id){
        Product product = productRepository.findByIdAndActiveTrue(id).orElse(null); 
        if(product == null){
            return GeneralResponde.<Void>builder()
            .status(HttpStatus.NOT_FOUND.value())
            .data(null).msg("Product not found").build();
        }
        product.setActive(false);
        productRepository.save(product);
        return GeneralResponde.<Void>builder()
                                .status(HttpStatus.OK.value())
                                .msg("Product deleted successfully")
                                .build(); 
    }
}
