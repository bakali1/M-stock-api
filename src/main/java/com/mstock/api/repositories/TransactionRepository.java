package com.mstock.api.repositories;

import com.mstock.api.Enum.TransactionTypeEnum;
import com.mstock.api.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * TransactionRepository - JPA repository for Transaction entity
 * Provides database access methods for transaction records (immutable after creation)
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find all transactions for a specific batch
     */
    List<Transaction> findByBatchId(Long batchId);

    /**
     * Find all transactions by user
     */
    List<Transaction> findByUserId(Long userId);

    /**
     * Find all transactions for a specific product
     */
    List<Transaction> findByProductId(Long productId);

    /**
     * Find transactions by type (IN|OUT|RETURN)
     */
    List<Transaction> findByType(TransactionTypeEnum type);

    /**
     * Find transactions within a date range (for audit filtering)
     */
    List<Transaction> findByCreatedAtBetweenOrderByCreatedAtDesc(Instant startDate, Instant endDate);

    /**
     * Find transaction by ID without loading relationships
     */
    @Query("SELECT t FROM Transaction t WHERE t.id = :id")
    Optional<Transaction> findByIdWithoutRelations(@Param("id") Long id);

    /**
     * Count all transactions for a batch
     */
    long countByBatchId(Long batchId);

    /**
     * Get transaction history for a product (all movements)
     */
    @Query("SELECT t FROM Transaction t WHERE t.product.id = :productId ORDER BY t.createdAt DESC")
    List<Transaction> getProductTransactionHistory(@Param("productId") Long productId);

    /**
     * Get transactions for a batch ordered by date (for batch history)
     */
    @Query("SELECT t FROM Transaction t WHERE t.batch.id = :batchId ORDER BY t.createdAt DESC")
    List<Transaction> getBatchTransactionHistory(@Param("batchId") Long batchId);
}

