package com.mstock.api.services;

import com.mstock.api.DTO.TransactionDTO;
import com.mstock.api.payload.Responde.GeneralResponde;
import com.mstock.api.payload.Request.TransactionRequest;

import java.time.Instant;
import java.util.List;

/**
 * TransactionService Interface
 * Defines all operations for transaction management
 * Note: Transactions are immutable after creation
 */
public interface TransactionService {

    /**
     * Create a new transaction (record stock movement)
     * Automatically updates batch quantity based on transaction type
     */
    GeneralResponde<?> createTransaction(TransactionRequest transactionRequest);

    /**
     * Get all transactions
     */
    GeneralResponde<List<TransactionDTO>> getAllTransactions();

    /**
     * Get specific transaction by ID
     */
    GeneralResponde<?> getTransaction(Long id);

    /**
     * Get transaction history for a batch (all movements)
     */
    GeneralResponde<List<TransactionDTO>> getTransactionsByBatch(Long batchId);

    /**
     * Get all transactions by user
     */
    GeneralResponde<List<TransactionDTO>> getTransactionsByUser(Long userId);

    /**
     * Get transaction history with date range and filters
     */
    GeneralResponde<List<TransactionDTO>> getTransactionHistory(
            Long productId,
            Instant startDate,
            Instant endDate
    );

    /**
     * Note: Transactions are immutable - no update or delete operations
     */
}
