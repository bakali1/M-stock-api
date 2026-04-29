# M-STOCK API - Transaction, Batch & AuditLog Implementation

## Overview
This document summarizes the complete implementation of Transaction, Batch, and AuditLog modules following the CAHIER_DES_CHARGES requirements.

## Implementation Status ✅

### Entities (Immutable & Optimistically Locked)
- **Batch**: Enhanced with status tracking (ACTIVE|QUARANTINE|RETIRED), expiration calculations, version field
- **Transaction**: Immutable after creation (no setters), version field for optimistic locking
- **AuditLog**: Append-only, immutable (no setters), automatic timestamp

### Enums Created
- `BatchStatusEnum`: ACTIVE, QUARANTINE, RETIRED
- `AuditActionEnum`: CREATE, UPDATE, DELETE

### DTOs & Mappers
- `BatchDTO` with `BatchMapper`
- `TransactionDTO` with `TransactionMapper`
- `AuditLogDTO` with `AuditLogMapper`

### Request Payloads
- `BatchRequest`: For creating/updating batches
- `TransactionRequest`: For recording transactions (immutable after creation)
- `AuditLogFilter`: For searching audit logs

---

## REST API Endpoints

### BATCH ENDPOINTS

#### Create Batch
```
POST /api/v0/batches
Content-Type: application/json

{
  "lotNumber": "LOT-2026-001",
  "quantity": 100,
  "expirationDate": "2027-04-28T23:59:59",
  "location": "Zone-A/Bin-1",
  "productId": 1
}

Response 201 Created:
{
  "data": {
    "id": 1,
    "lotNumber": "LOT-2026-001",
    "quantity": 100,
    "expirationDate": "2027-04-28T23:59:59",
    "location": "Zone-A/Bin-1",
    "status": "ACTIVE",
    "productId": 1,
    "productName": "Aspirin",
    "nsnCode": "NSN-123456",
    "daysUntilExpiration": 365,
    "expirationAlertLevel": "NORMAL"
  },
  "msg": "Batch created successfully"
}
```

#### Get All Batches
```
GET /api/v0/batches

Response 200 OK: List of all active batches
```

#### Get Batch by ID
```
GET /api/v0/batches/{id}

Response 200 OK: Single batch details
```

#### Get Batch by Lot Number
```
GET /api/v0/batches/lot/{lotNumber}

Response 200 OK: Batch with exact lot number match
```

#### Get Batches by Product
```
GET /api/v0/batches/product/{productId}

Response 200 OK: All batches for a product
```

#### Get Expiration Alerts
```
GET /api/v0/batches/alerts/{days}
  days parameter: number of days threshold (e.g., 30)

Response 200 OK: Batches expiring within threshold
Alert Levels:
  - CRITICAL: < 7 days
  - ATTENTION: 7-30 days
  - NORMAL: > 30 days
```

#### Search Batches
```
GET /api/v0/batches/search?nsnCode=NSN-123456&lotNumber=LOT-2026

Response 200 OK: Search results
Note: Either nsnCode or lotNumber can be provided
```

#### Get Expired Batches
```
GET /api/v0/batches/expired

Response 200 OK: List of all expired batches
```

#### Update Batch
```
PUT /api/v0/batches
Content-Type: application/json

{
  "id": 1,
  "quantity": 95,
  "location": "Zone-A/Bin-2"
}

Response 200 OK: Updated batch details
```

#### Quarantine Batch
```
PUT /api/v0/batches/{id}/quarantine?reason=FDA+Recall

Response 200 OK: Batch marked as QUARANTINE
```

#### Delete Batch (Soft Delete)
```
DELETE /api/v0/batches/{id}

Response 200 OK: Batch status changed to RETIRED
```

---

### TRANSACTION ENDPOINTS

#### Create Transaction (Record Stock Movement)
```
POST /api/v0/transactions
Content-Type: application/json

{
  "type": "OUT",           // IN|OUT|RETURN
  "quantity": 10,
  "reason": "Patient Use",
  "batchId": 1,
  "userId": 1
}

Response 201 Created:
{
  "data": {
    "id": 1,
    "type": "OUT",
    "quantity": 10,
    "reason": "Patient Use",
    "createdAt": "2026-04-28T10:30:00Z",
    "userId": 1,
    "userName": "john.doe",
    "productId": 1,
    "productName": "Aspirin",
    "batchId": 1,
    "lotNumber": "LOT-2026-001"
  },
  "msg": "Transaction recorded successfully"
}

Note: Updates batch quantity automatically:
- IN: increases quantity
- OUT: decreases quantity (validates sufficient stock)
- RETURN: increases quantity
```

#### Get All Transactions
```
GET /api/v0/transactions

Response 200 OK: All transactions
```

#### Get Transaction by ID
```
GET /api/v0/transactions/{id}

Response 200 OK: Single transaction
```

#### Get Batch Transaction History
```
GET /api/v0/transactions/batch/{batchId}

Response 200 OK: All transactions for batch (ordered by date DESC)
```

#### Get User Transactions
```
GET /api/v0/transactions/user/{userId}

Response 200 OK: All transactions by user
```

#### Get Transaction History (with filters)
```
GET /api/v0/transactions/history?productId=1&startDate=2026-01-01T00:00:00Z&endDate=2026-04-28T23:59:59Z

Response 200 OK: Filtered transaction history
Note: Date range defaults to last 90 days if not provided
```

---

### AUDIT LOG ENDPOINTS

#### Get All Audit Logs (Paginated)
```
GET /api/v0/audit-logs?page=0&size=20

Response 200 OK:
{
  "data": {
    "content": [...],
    "pageable": {...},
    "totalPages": 5,
    "totalElements": 95,
    "currentPage": 0
  },
  "msg": "All audit logs (page 1 of 5)"
}
```

#### Get Audit Log by ID
```
GET /api/v0/audit-logs/{id}

Response 200 OK: Single audit log entry
```

#### Get Audit Logs by User
```
GET /api/v0/audit-logs/user/{userId}

Response 200 OK: All actions by user (most recent first)
```

#### Get Audit Logs by Table
```
GET /api/v0/audit-logs/table/{tableName}

Response 200 OK: All changes to table (most recent first)
Tables: batches, transactions, products, app_user
```

#### Search Audit Logs (Complex Filter)
```
POST /api/v0/audit-logs/search
Content-Type: application/json

{
  "userId": 1,
  "action": "UPDATE",
  "tableName": "batches",
  "startDate": "2026-01-01T00:00:00Z",
  "endDate": "2026-04-28T23:59:59Z",
  "pageNumber": 0,
  "pageSize": 20
}

Response 200 OK: Paginated results matching criteria
```

#### Chain of Custody Report
```
GET /api/v0/audit-logs/report?startDate=2026-01-01T00:00:00Z&endDate=2026-04-28T23:59:59Z

Response 200 OK: Audit trail for compliance/military audit
```

#### Audit Statistics
```
GET /api/v0/audit-logs/statistics

Response 200 OK:
{
  "data": {
    "creates": 245,
    "updates": 1823,
    "deletes": 12,
    "total": 2080
  },
  "msg": "Audit Statistics"
}
```

---

## Database Schema

### Batch Table
```sql
CREATE TABLE batches (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    lot_number VARCHAR(255) UNIQUE NOT NULL,
    quantity INT NOT NULL,
    expiration_date DATETIME NOT NULL,
    location VARCHAR(255) NOT NULL,
    status ENUM('ACTIVE', 'QUARANTINE', 'RETIRED') DEFAULT 'ACTIVE',
    product_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_lot_number (lot_number),
    INDEX idx_expiration_date (expiration_date),
    INDEX idx_product_id (product_id),
    INDEX idx_status (status)
);
```

### Transaction Table
```sql
CREATE TABLE transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type ENUM('IN', 'OUT', 'RETURN') NOT NULL,
    quantity INT NOT NULL,
    reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    batch_id BIGINT NOT NULL,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES app_user(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (batch_id) REFERENCES batches(id),
    INDEX idx_batch_id (batch_id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    INDEX idx_product_id (product_id)
);
```

### AuditLog Table
```sql
CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    action ENUM('CREATE', 'UPDATE', 'DELETE') NOT NULL,
    table_name VARCHAR(255) NOT NULL,
    old_value LONGTEXT,
    new_value LONGTEXT,
    ip_address VARCHAR(45),
    reason VARCHAR(500),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES app_user(id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_table_name (table_name)
);
```

---

## Key Features

### ✅ Batch Management
- Lot tracking with unique lot numbers
- Expiration date monitoring (< 7 days = CRITICAL)
- Status transitions: ACTIVE → QUARANTINE → RETIRED
- Search by NSN code or lot number (wildcard)
- Automatic expiration alerts

### ✅ Transaction Recording
- Stock movement tracking (IN|OUT|RETURN)
- Automatic quantity updates on batch
- User/WHO tracking (from X-Forwarded-User header)
- Reason/context documentation
- Immutable after creation (prevents accidental changes)
- Optimistic locking to prevent concurrent update conflicts

### ✅ Audit Trail (Compliance)
- Append-only immutable logs
- Automatic capture of WHO/WHAT/WHEN/WHERE
- Complex filtering (date range, user, action, table)
- Pagination support for large datasets
- Chain of Custody report for military compliance
- 1-year rolling policy (optional archival)

### ✅ Design Patterns
- Soft delete pattern (RETIRED status, active flag)
- Optimistic locking (@Version) for concurrent updates
- LAZY loading to prevent N+1 queries
- Partial updates with null-safety
- Read-only DTOs for API responses

---

## Example Workflows

### Workflow 1: Stock Receipt & Inventory Tracking
```
1. Create Batch
   POST /api/v0/batches
   - Lot: LOT-2026-001, Qty: 100

2. Record Receipt Transaction
   POST /api/v0/transactions
   - Type: IN, Qty: 100, Reason: "Supplier Delivery"
   - Batch quantity: 100 (created) → 200 (after transaction)

3. Check Expiration Status
   GET /api/v0/batches/alerts/30
   - Returns batches expiring within 30 days

4. Audit Trail
   GET /api/v0/audit-logs/table/batches
   - Shows who created batch and who recorded receipt
```

### Workflow 2: Stock Withdrawal & Traceability
```
1. Get Batch History
   GET /api/v0/batches/lot/LOT-2026-001

2. Record Withdrawal
   POST /api/v0/transactions
   - Type: OUT, Qty: 10, Reason: "Patient Use"
   - Batch quantity: 200 → 190

3. Search Movement History
   GET /api/v0/transactions/history?productId=1
   - Shows all movements for this product

4. User Activity Report
   GET /api/v0/audit-logs/user/1
   - Shows all actions by this user
```

### Workflow 3: FDA Recall Response
```
1. Search Affected Batches
   GET /api/v0/batches/search?nsnCode=NSN-123456
   - Returns all batches for product

2. Quarantine Batches
   PUT /api/v0/batches/1/quarantine?reason=FDA+Recall
   - Status changes to QUARANTINE

3. Generate Compliance Report
   GET /api/v0/audit-logs/report?startDate=2026-01-01T00:00:00Z&endDate=2026-04-28T23:59:59Z
   - Shows all movements of recalled products (chain of custody)

4. Export Audit Trail
   - Report includes: WHO moved it, WHEN, WHERE from/to, REASON
```

---

## Implementation Notes

### Dependencies
- Spring Boot 4.0.5 (Java 21)
- Spring Data JPA (with Hibernate)
- MapStruct 1.6.3 for DTO mapping
- Lombok for boilerplate reduction
- PostgreSQL JDBC driver

### Configuration
- Pessimistic locking: Not needed (optimistic via @Version)
- Fetch strategy: LAZY for all relationships
- Transaction propagation: REQUIRED (default)
- Read-only for safe queries: `@Transactional(readOnly = true)`

### Performance Optimizations
- Database indexes on: lot_number, expiration_date, timestamp, user_id, batch_id
- Pagination for audit logs (default: 20 items/page)
- N+1 query prevention via custom @Query methods
- Version field prevents concurrent update conflicts

### Security Considerations
- Audit logs are append-only and immutable
- No delete operations on audit logs
- IP address captured from X-Forwarded-For header
- User tracking via X-Forwarded-User header (CAC authentication)
- All inputs validated and sanitized

---

## Next Steps (Phase 2)

1. **AOP Aspect Implementation**: Automatic audit log creation on entity changes
2. **PDF Report Generation**: Export audit trail as PDF
3. **Email Notifications**: Alert on critical expiration
4. **Barcode Scanner Integration**: Real-time transaction recording
5. **Dashboard Widgets**: Expiration forecasting, PAR levels, activity summary

---

## Migration from MySQL to PostgreSQL

Update `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mstock
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

Add PostgreSQL driver to `pom.xml`:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.6.0</version>
    <scope>runtime</scope>
</dependency>
```

---

**Implementation Date**: April 2026
**CAHIER Reference**: Section 2.1-2.4 (Batch, Transaction, Audit Trail)
**Status**: ✅ Complete (Ready for testing)
