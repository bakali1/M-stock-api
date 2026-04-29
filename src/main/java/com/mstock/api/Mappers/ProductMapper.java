package com.mstock.api.Mappers;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.mstock.api.DTO.ProductDTO;
import com.mstock.api.entities.Product;
import com.mstock.api.payload.Request.ProductRequest;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @BeforeMapping
    default void normalizeRequest(ProductRequest request) {
        // Use reflection or manual checking for each string field
        if (request.getName() != null && request.getName().trim().isEmpty()) {
            request.setName(null);
        }
        if (request.getDescription() != null && request.getDescription().trim().isEmpty()) {
            request.setDescription(null);
        }
        if (request.getNsnCode() != null && request.getNsnCode().trim().isEmpty()) {
            request.setNsnCode(null);
        }
    }
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "batches", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateProductFromRequest(ProductRequest request, @MappingTarget Product product);

    @BeanMapping(resultType=ProductDTO.class,nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ProductDTO toProductDTO(Product product);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    List<ProductDTO> toProductDTO(List<Product> products);
}



