package com.mstock.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mstock.api.entities.Product;
import java.util.List;
import java.util.Optional;


@Repository
public interface ProductRepository extends JpaRepository<Product,Long>{
    List<Product> findByActiveTrue();
    Optional<Product> findByIdAndActiveTrue(long id);
    boolean existsByNsnCode(String nsnCode);
    
    /**
     * Find product by ID without loading relationships (batches, transactions)
     * Used for update operations to avoid N+1 query problems
     */
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithoutRelations(@Param("id") Long id);
    
    /**
     * Find active product by ID without loading relationships
     * Used for get/read operations to avoid N+1 query problems
     */
    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.active = true")
    Optional<Product> findByIdAndActiveTrueWithoutRelations(@Param("id") Long id);
}

