package com.mstock.api.repositories;

import com.mstock.api.Enum.BatchStatusEnum;
import com.mstock.api.entities.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * BatchRepository - JPA repository for Batch entity
 * Provides database access methods for batch operations
 */
@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {

    /**
     * Find batch by lot number (exact match)
     */
    Optional<Batch> findByLotNumber(String lotNumber);

    /**
     * Check if lot number already exists
     */
    boolean existsByLotNumber(String lotNumber);

    /**
     * Find all batches by product ID
     */
    List<Batch> findByProductId(Long productId);

    /**
     * Find all active batches
     */
    List<Batch> findByStatus(BatchStatusEnum status);

    /**
     * Find batches by expiration date range - for expiration forecasting
     */
    List<Batch> findByExpirationDateBetweenAndStatus(
            LocalDateTime startDate,
            LocalDateTime endDate,
            BatchStatusEnum status
    );

    /**
     * Find batches with specific status before a given date
     */
    List<Batch> findByExpirationDateBeforeAndStatus(
            LocalDateTime expirationDate,
            BatchStatusEnum status
    );

    /**
     * Find batch by ID without loading relationships
     */
    @Query("SELECT b FROM Batch b WHERE b.id = :id")
    Optional<Batch> findByIdWithoutRelations(@Param("id") Long id);

    /**
     * Find batch by ID with specific status
     */
    Optional<Batch> findByIdAndStatus(Long id, BatchStatusEnum status);

    /**
     * Count batches by product ID and status
     */
    long countByProductIdAndStatus(Long productId, BatchStatusEnum status);

    /**
     * Search batches by lot number (partial match)
     */
    @Query("SELECT b FROM Batch b WHERE LOWER(b.lotNumber) LIKE LOWER(CONCAT('%', :lotNumber, '%')) AND b.status = :status")
    List<Batch> searchByLotNumberLike(@Param("lotNumber") String lotNumber, @Param("status") BatchStatusEnum status);

    /**
     * Search batches by product NSN code and status
     */
    @Query("SELECT b FROM Batch b WHERE b.product.nsnCode = :nsnCode AND b.status = :status")
    List<Batch> findByProductNsnCodeAndStatus(@Param("nsnCode") String nsnCode, @Param("status") BatchStatusEnum status);
}
