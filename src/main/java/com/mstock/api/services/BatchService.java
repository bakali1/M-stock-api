package com.mstock.api.services;

import com.mstock.api.DTO.BatchDTO;
import com.mstock.api.payload.Responde.GeneralResponde;
import com.mstock.api.payload.Request.BatchRequest;

import java.util.List;

/**
 * BatchService Interface
 * Defines all operations for batch management
 */
public interface BatchService {

    /**
     * Create a new batch
     */
    GeneralResponde<?> createBatch(BatchRequest batchRequest);

    /**
     * Get all active batches
     */
    GeneralResponde<List<BatchDTO>> getAllBatches();

    /**
     * Get specific batch by ID
     */
    GeneralResponde<?> getBatch(Long id);

    /**
     * Update an existing batch (partial update)
     */
    GeneralResponde<?> updateBatch(BatchRequest batchRequest);

    /**
     * Soft delete a batch (change status to RETIRED)
     */
    GeneralResponde<?> deleteBatch(Long id);

    /**
     * Get batch by lot number (exact match)
     */
    GeneralResponde<?> getBatchByLotNumber(String lotNumber);

    /**
     * Get all batches for a specific product
     */
    GeneralResponde<List<BatchDTO>> getBatchesByProductId(Long productId);

    /**
     * Get batches with expiration alerts (< 7 days = CRITICAL, 7-30 days = ATTENTION)
     */
    GeneralResponde<List<BatchDTO>> getExpirationAlerts(Integer daysThreshold);

    /**
     * Search batches by NSN code or lot number (wildcard search)
     */
    GeneralResponde<List<BatchDTO>> searchBatches(String nsnCode, String lotNumber);

    /**
     * Quarantine a batch (mark as QUARANTINE status)
     */
    GeneralResponde<?> quarantineBatch(Long batchId, String reason);

    /**
     * Get all expired batches
     */
    GeneralResponde<List<BatchDTO>> getExpiredBatches();
}
