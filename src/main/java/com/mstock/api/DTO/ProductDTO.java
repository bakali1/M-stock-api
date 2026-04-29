package com.mstock.api.DTO;

import com.mstock.api.entities.Product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDTO {
    private long id;
    private String name;
    private String nsnCode;
    private String description;
    private int parLevel;
    @Default
    private boolean active = true;

    public ProductDTO(Product product){
        this( product.getId(), product.getName(), product.getNsnCode(),
              product.getDescription(), product.getParLevel(),
              product.isActive());
    }
}
