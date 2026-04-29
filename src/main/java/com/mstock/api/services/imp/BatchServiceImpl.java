package com.mstock.api.services.imp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mstock.api.DTO.BatchDTO;
import com.mstock.api.Enum.BatchStatusEnum;
import com.mstock.api.entities.Batch;
import com.mstock.api.entities.Product;
import com.mstock.api.payload.Request.BatchRequest;
import com.mstock.api.payload.Responde.GeneralResponde;
import com.mstock.api.repositories.BatchRepository;
import com.mstock.api.repositories.ProductRepository;
import com.mstock.api.services.BatchService;

import lombok.RequiredArgsConstructor;

/**
 * BatchServiceImpl - Implementation of BatchService
 * Handles all business logic for batch management including expiration forecasting
 */
@Service
@RequiredArgsConstructor
public class BatchServiceImpl implements BatchService {

    private final BatchRepository batchRepository;
    private final ProductRepository productRepository;
    @Override
    @Transactional
    public GeneralResponde<?> createBatch(BatchRequest batchRequest) {
        // Validate product exists
        if (batchRequest.getProductId() == null) {
            return GeneralResponde.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .msg("Product ID is required")
                    .build();
        }

        Product product = productRepository.findByIdAndActiveTrueWithoutRelations(batchRequest.getProductId())
                .orElse(null);
        if (product == null) {
            return GeneralResponde.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .msg("Product not found")
                    .build();
        }

        // Check for duplicate lot number
        if (batchRepository.existsByLotNumber(batchRequest.getLotNumber())) {
            return GeneralResponde.builder()
                    .status(HttpStatus.CONFLICT.value())
                    .msg("Batch with this lot number already exists")
                    .build();
        }

        // Validate required fields
        if (batchRequest.getQuantity() == null || batchRequest.getQuantity() <= 0) {
            return GeneralResponde.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .msg("Quantity must be greater than 0")
                    .build();
        }

        if (batchRequest.getExpirationDate() == null) {
            return GeneralResponde.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .msg("Expiration date is required")
                    .build();
        }

        // Create batch
        Batch batch = Batch.builder()
                .lotNumber(batchRequest.getLotNumber().trim())
                .quantity(batchRequest.getQuantity())
                .expirationDate(batchRequest.getExpirationDate())
                .location(batchRequest.getLocation().trim())
                .status(BatchStatusEnum.ACTIVE)
                .product(product)
                .build();

        Batch savedBatch = batchRepository.save(batch);
        BatchDTO batchDTO = new BatchDTO(savedBatch);

        return GeneralResponde.builder()
                .status(HttpStatus.CREATED.value())
                .msg("Batch created successfully")
                .data(batchDTO)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralResponde<List<BatchDTO>> getAllBatches() {
        List<Batch> batches = batchRepository.findByStatus(BatchStatusEnum.ACTIVE);
        List<BatchDTO> batchDTOs = batches.stream()
                .map(BatchDTO::new)
                .collect(Collectors.toList());

        return GeneralResponde.<List<BatchDTO>>builder()
                .status(HttpStatus.OK.value())
                .msg("All active batches")
                .data(batchDTOs)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralResponde<?> getBatch(Long id) {
        Batch batch = batchRepository.findByIdWithoutRelations(id).orElse(null);
        if (batch == null) {
            return GeneralResponde.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .msg("Batch not found")
                    .build();
        }

        BatchDTO batchDTO = new BatchDTO(batch);
        return GeneralResponde.builder()
                .status(HttpStatus.OK.value())
                .msg("Batch found")
                .data(batchDTO)
                .build();
    }

    @Override
    @Transactional
    public GeneralResponde<?> updateBatch(BatchRequest batchRequest) {
        if (batchRequest.getId() == null) {
            return GeneralResponde.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .msg("Batch ID is required for update")
                    .build();
        }

        Batch batch = batchRepository.findByIdWithoutRelations(batchRequest.getId()).orElse(null);
        if (batch == null) {
            return GeneralResponde.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .msg("Batch not found")
                    .build();
        }

        // Use entity's built-in partial update method
        batch.updateFromRequest(batchRequest);

        Batch updatedBatch = batchRepository.save(batch);
        BatchDTO batchDTO = new BatchDTO(updatedBatch);

        return GeneralResponde.builder()
                .status(HttpStatus.OK.value())
                .msg("Batch updated successfully")
                .data(batchDTO)
                .build();
    }

    @Override
    @Transactional
    public GeneralResponde<?> deleteBatch(Long id) {
        Batch batch = batchRepository.findByIdAndStatus(id, BatchStatusEnum.ACTIVE).orElse(null);
        if (batch == null) {
            return GeneralResponde.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .msg("Active batch not found")
                    .build();
        }

        // Soft delete: change status to RETIRED
        batch.setStatus(BatchStatusEnum.RETIRED);
        batchRepository.save(batch);

        return GeneralResponde.builder()
                .status(HttpStatus.OK.value())
                .msg("Batch deleted successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralResponde<?> getBatchByLotNumber(String lotNumber) {
        Batch batch = batchRepository.findByLotNumber(lotNumber).orElse(null);
        if (batch == null) {
            return GeneralResponde.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .msg("Batch with lot number not found")
                    .build();
        }

        BatchDTO batchDTO = new BatchDTO(batch);
        return GeneralResponde.builder()
                .status(HttpStatus.OK.value())
                .msg("Batch found")
                .data(batchDTO)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralResponde<List<BatchDTO>> getBatchesByProductId(Long productId) {
        Product product = productRepository.findByIdAndActiveTrueWithoutRelations(productId).orElse(null);
        if (product == null) {
            return GeneralResponde.<List<BatchDTO>>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .msg("Product not found")
                    .build();
        }

        List<Batch> batches = batchRepository.findByProductId(productId);
        List<BatchDTO> batchDTOs = batches.stream()
                .map(BatchDTO::new)
                .collect(Collectors.toList());

        return GeneralResponde.<List<BatchDTO>>builder()
                .status(HttpStatus.OK.value())
                .msg("Batches for product")
                .data(batchDTOs)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralResponde<List<BatchDTO>> getExpirationAlerts(Integer daysThreshold) {
        if (daysThreshold == null) {
            daysThreshold = 30; // Default threshold
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusDays(daysThreshold);

        List<Batch> batches = batchRepository.findByExpirationDateBetweenAndStatus(
                now,
                futureDate,
                BatchStatusEnum.ACTIVE
        );

        List<BatchDTO> batchDTOs = batches.stream()
                .map(BatchDTO::new)
                .collect(Collectors.toList());

        return GeneralResponde.<List<BatchDTO>>builder()
                .status(HttpStatus.OK.value())
                .msg("Batches with expiration alerts")
                .data(batchDTOs)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralResponde<List<BatchDTO>> searchBatches(String nsnCode, String lotNumber) {
        List<Batch> batches = List.of();

        if (nsnCode != null && !nsnCode.isEmpty()) {
            batches = batchRepository.findByProductNsnCodeAndStatus(nsnCode, BatchStatusEnum.ACTIVE);
        } else if (lotNumber != null && !lotNumber.isEmpty()) {
            batches = batchRepository.searchByLotNumberLike(lotNumber, BatchStatusEnum.ACTIVE);
        }

        List<BatchDTO> batchDTOs = batches.stream()
                .map(BatchDTO::new)
                .collect(Collectors.toList());

        return GeneralResponde.<List<BatchDTO>>builder()
                .status(HttpStatus.OK.value())
                .msg("Search results")
                .data(batchDTOs)
                .build();
    }

    @Override
    @Transactional
    public GeneralResponde<?> quarantineBatch(Long batchId, String reason) {
        Batch batch = batchRepository.findByIdAndStatus(batchId, BatchStatusEnum.ACTIVE).orElse(null);
        if (batch == null) {
            return GeneralResponde.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .msg("Active batch not found")
                    .build();
        }

        batch.setStatus(BatchStatusEnum.QUARANTINE);
        batchRepository.save(batch);

        return GeneralResponde.builder()
                .status(HttpStatus.OK.value())
                .msg("Batch quarantined successfully: " + reason)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralResponde<List<BatchDTO>> getExpiredBatches() {
        LocalDateTime now = LocalDateTime.now();
        List<Batch> batches = batchRepository.findByExpirationDateBeforeAndStatus(now, BatchStatusEnum.ACTIVE);

        List<BatchDTO> batchDTOs = batches.stream()
                .map(BatchDTO::new)
                .collect(Collectors.toList());

        return GeneralResponde.<List<BatchDTO>>builder()
                .status(HttpStatus.OK.value())
                .msg("Expired batches")
                .data(batchDTOs)
                .build();
    }
}
