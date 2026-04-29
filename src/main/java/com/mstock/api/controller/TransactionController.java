package com.mstock.api.controller;

import com.mstock.api.payload.Responde.GeneralResponde;
import com.mstock.api.payload.Request.TransactionRequest;
import com.mstock.api.services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * TransactionController - REST endpoints for transaction management
 * Records stock movements (IN|OUT|RETURN)
 * Note: Transactions are immutable after creation (read-only after POST)
 */
@RestController
@RequestMapping("/api/v0/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<GeneralResponde<?>> createTransaction(@RequestBody TransactionRequest request) {
        GeneralResponde<?> response = transactionService.createTransaction(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping
    public ResponseEntity<GeneralResponde<?>> getAllTransactions() {
        GeneralResponde<?> response = transactionService.getAllTransactions();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponde<?>> getTransaction(@PathVariable Long id) {
        GeneralResponde<?> response = transactionService.getTransaction(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/batch/{batchId}")
    public ResponseEntity<GeneralResponde<?>> getTransactionsByBatch(@PathVariable Long batchId) {
        GeneralResponde<?> response = transactionService.getTransactionsByBatch(batchId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<GeneralResponde<?>> getTransactionsByUser(@PathVariable Long userId) {
        GeneralResponde<?> response = transactionService.getTransactionsByUser(userId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/history")
    public ResponseEntity<GeneralResponde<?>> getTransactionHistory(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        java.time.Instant start = null;
        java.time.Instant end = null;

        if (startDate != null && !startDate.isEmpty()) {
            start = java.time.Instant.parse(startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            end = java.time.Instant.parse(endDate);
        }

        GeneralResponde<?> response = transactionService.getTransactionHistory(productId, start, end);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
