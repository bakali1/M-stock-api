package com.mstock.api.controller;

import com.mstock.api.payload.Responde.GeneralResponde;
import com.mstock.api.payload.Request.BatchRequest;
import com.mstock.api.services.BatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * BatchController - REST endpoints for batch management
 * Provides CRUD operations and specialized queries for batches
 */
@RestController
@RequestMapping("/api/v0/batches")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;

    @PostMapping
    public ResponseEntity<GeneralResponde<?>> createBatch(@RequestBody BatchRequest request) {
        GeneralResponde<?> response = batchService.createBatch(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping
    public ResponseEntity<GeneralResponde<?>> getAllBatches() {
        GeneralResponde<?> response = batchService.getAllBatches();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponde<?>> getBatch(@PathVariable Long id) {
        GeneralResponde<?> response = batchService.getBatch(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/lot/{lotNumber}")
    public ResponseEntity<GeneralResponde<?>> getBatchByLotNumber(@PathVariable String lotNumber) {
        GeneralResponde<?> response = batchService.getBatchByLotNumber(lotNumber);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<GeneralResponde<?>> getBatchesByProductId(@PathVariable Long productId) {
        GeneralResponde<?> response = batchService.getBatchesByProductId(productId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/alerts/{days}")
    public ResponseEntity<GeneralResponde<?>> getExpirationAlerts(@PathVariable Integer days) {
        GeneralResponde<?> response = batchService.getExpirationAlerts(days);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/search")
    public ResponseEntity<GeneralResponde<?>> searchBatches(
            @RequestParam(required = false) String nsnCode,
            @RequestParam(required = false) String lotNumber) {
        GeneralResponde<?> response = batchService.searchBatches(nsnCode, lotNumber);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/expired")
    public ResponseEntity<GeneralResponde<?>> getExpiredBatches() {
        GeneralResponde<?> response = batchService.getExpiredBatches();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping
    public ResponseEntity<GeneralResponde<?>> updateBatch(@RequestBody BatchRequest request) {
        GeneralResponde<?> response = batchService.updateBatch(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping("/{id}/quarantine")
    public ResponseEntity<GeneralResponde<?>> quarantineBatch(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        GeneralResponde<?> response = batchService.quarantineBatch(id, reason);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponde<?>> deleteBatch(@PathVariable Long id) {
        GeneralResponde<?> response = batchService.deleteBatch(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
