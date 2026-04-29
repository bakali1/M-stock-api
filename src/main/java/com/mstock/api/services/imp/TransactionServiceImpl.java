package com.mstock.api.services.imp;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mstock.api.DTO.TransactionDTO;
import com.mstock.api.Enum.TransactionTypeEnum;
import com.mstock.api.entities.Batch;
import com.mstock.api.entities.Transaction;
import com.mstock.api.entities.User;
import com.mstock.api.payload.Request.TransactionRequest;
import com.mstock.api.payload.Responde.GeneralResponde;
import com.mstock.api.repositories.BatchRepository;
import com.mstock.api.repositories.TransactionRepository;
import com.mstock.api.repositories.UserRepository;
import com.mstock.api.services.TransactionService;

import lombok.RequiredArgsConstructor;

/**
 * TransactionServiceImpl - Implementation of TransactionService
 * Handles stock movement tracking and quantity updates
 * Transactions are immutable after creation (optimistic locking via @Version)
 */
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final BatchRepository batchRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public GeneralResponde<?> createTransaction(TransactionRequest transactionRequest) {
        // Validate required fields
        if (transactionRequest.getType() == null) {
            return GeneralResponde.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .msg("Transaction type is required (IN|OUT|RETURN)")
                    .build();
        }

        if (transactionRequest.getQuantity() == null || transactionRequest.getQuantity() <= 0) {
            return GeneralResponde.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .msg("Quantity must be greater than 0")
                    .build();
        }

        if (transactionRequest.getBatchId() == null) {
            return GeneralResponde.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .msg("Batch ID is required")
                    .build();
        }

        // Find batch
        Batch batch = batchRepository.findByIdWithoutRelations(transactionRequest.getBatchId()).orElse(null);
        if (batch == null) {
            return GeneralResponde.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .msg("Batch not found")
                    .build();
        }

        // Find user (if not provided, use default or error)
        User user = null;
        if (transactionRequest.getUserId() != null) {
            user = userRepository.findByIdWithoutRelations(transactionRequest.getUserId()).orElse(null);
        }
        if (user == null) {
            return GeneralResponde.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .msg("User not found")
                    .build();
        }

        // Validate quantity for OUT transactions
        if (transactionRequest.getType() == TransactionTypeEnum.OUT) {
            if (batch.getQuantity() < transactionRequest.getQuantity()) {
                return GeneralResponde.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .msg("Insufficient quantity available. Available: " + batch.getQuantity())
                        .build();
            }
        }

        // Create transaction (immutable after creation)
        Transaction transaction = Transaction.builder()
                .type(transactionRequest.getType())
                .quantity(transactionRequest.getQuantity())
                .reason(transactionRequest.getReason())
                .user(user)
                .product(batch.getProduct())
                .batch(batch)
                .build();

        // Update batch quantity based on transaction type
        switch (transactionRequest.getType()) {
            case TransactionTypeEnum.IN:
                batch.setQuantity(batch.getQuantity() + transactionRequest.getQuantity());
                break;
            case TransactionTypeEnum.OUT:
                batch.setQuantity(batch.getQuantity() - transactionRequest.getQuantity());
                break;
            case TransactionTypeEnum.RETURN:
                batch.setQuantity(batch.getQuantity() + transactionRequest.getQuantity());
                break;
        }

        // Save both transaction and updated batch
        Transaction savedTransaction = transactionRepository.save(transaction);
        batchRepository.save(batch);

        TransactionDTO transactionDTO = new TransactionDTO(savedTransaction);

        return GeneralResponde.builder()
                .status(HttpStatus.CREATED.value())
                .msg("Transaction recorded successfully")
                .data(transactionDTO)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralResponde<List<TransactionDTO>> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAll();
        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(TransactionDTO::new)
                .collect(Collectors.toList());

        return GeneralResponde.<List<TransactionDTO>>builder()
                .status(HttpStatus.OK.value())
                .msg("All transactions")
                .data(transactionDTOs)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralResponde<?> getTransaction(Long id) {
        Transaction transaction = transactionRepository.findByIdWithoutRelations(id).orElse(null);
        if (transaction == null) {
            return GeneralResponde.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .msg("Transaction not found")
                    .build();
        }

        TransactionDTO transactionDTO = new TransactionDTO(transaction);
        return GeneralResponde.builder()
                .status(HttpStatus.OK.value())
                .msg("Transaction found")
                .data(transactionDTO)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralResponde<List<TransactionDTO>> getTransactionsByBatch(Long batchId) {
        Batch batch = batchRepository.findByIdWithoutRelations(batchId).orElse(null);
        if (batch == null) {
            return GeneralResponde.<List<TransactionDTO>>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .msg("Batch not found")
                    .build();
        }

        List<Transaction> transactions = transactionRepository.getBatchTransactionHistory(batchId);
        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(TransactionDTO::new)
                .collect(Collectors.toList());

        return GeneralResponde.<List<TransactionDTO>>builder()
                .status(HttpStatus.OK.value())
                .msg("Batch transaction history")
                .data(transactionDTOs)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralResponde<List<TransactionDTO>> getTransactionsByUser(Long userId) {
        User user = userRepository.findByIdWithoutRelations(userId).orElse(null);
        if (user == null) {
            return GeneralResponde.<List<TransactionDTO>>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .msg("User not found")
                    .build();
        }

        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(TransactionDTO::new)
                .collect(Collectors.toList());

        return GeneralResponde.<List<TransactionDTO>>builder()
                .status(HttpStatus.OK.value())
                .msg("User transactions")
                .data(transactionDTOs)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralResponde<List<TransactionDTO>> getTransactionHistory(
            Long productId,
            Instant startDate,
            Instant endDate) {

        // If no date range provided, use default (last 90 days)
        if (startDate == null) {
            startDate = Instant.now().minusSeconds(90 * 24 * 60 * 60);
        }
        if (endDate == null) {
            endDate = Instant.now();
        }

        List<Transaction> transactions = transactionRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);

        // Filter by product if provided
        if (productId != null) {
            transactions = transactions.stream()
                    .filter(t -> t.getProduct().getId().equals(productId))
                    .collect(Collectors.toList());
        }

        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(TransactionDTO::new)
                .collect(Collectors.toList());

        return GeneralResponde.<List<TransactionDTO>>builder()
                .status(HttpStatus.OK.value())
                .msg("Transaction history")
                .data(transactionDTOs)
                .build();
    }
}
