package com.mstock.api.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mstock.api.payload.Request.ProductRequest;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(unique = true, nullable = false)
    private String nsnCode;
    private String description;
    private Integer parLevel;
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Batch> batches;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Transaction> transactions;

    @Version
    private Long version;
    
    /**
     * Partial update method that only updates non-null and non-empty fields from ProductRequest
     * Empty strings are treated as "no update" and will not overwrite existing values
     * 
     * @param request ProductRequest containing fields to update
     */
    public void updateFromRequest(ProductRequest request) {
        // Update name only if provided and not empty
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            this.name = request.getName().trim();
        }
        
        // Update nsnCode only if provided and not empty
        if (request.getNsnCode() != null && !request.getNsnCode().trim().isEmpty()) {
            this.nsnCode = request.getNsnCode().trim();
        }
        
        // Update description only if provided and not empty
        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            this.description = request.getDescription().trim();
        }
        
        // Update parLevel only if provided (0 is a valid value)
        if (request.getParLevel() != null) {
            this.parLevel = request.getParLevel();
        }
        
        // Update active only if provided
        if (request.getActive() != null) {
            this.active = request.getActive();
        }
    }
}

