# M-Stock API - Comprehensive Exploration Report

**Generated:** April 29, 2026  
**Project Location:** `/home/baku/programing/M-stock/api`  
**Status:** Early stage MVP, actively developed  
**Language:** Java 21 | **Framework:** Spring Boot 4.0.6 | **Database:** PostgreSQL 15

---

## 1. PROJECT STRUCTURE

### Directory Layout
```
M-stock/api/
├── src/
│   ├── main/java/com/mstock/api/
│   │   ├── ApiApplication.java                    # Spring Boot entry point
│   │   ├── controller/                            # REST endpoints (5 controllers)
│   │   │   ├── ProductController.java
│   │   │   ├── BatchController.java
│   │   │   ├── TransactionController.java
│   │   │   ├── AuditLogController.java
│   │   │   └── UserController.java
│   │   ├── entities/                              # JPA entities (5 entities)
│   │   │   ├── User.java
│   │   │   ├── Product.java
│   │   │   ├── Batch.java
│   │   │   ├── Transaction.java
│   │   │   └── AuditLog.java
│   │   ├── Enum/                                  # Enums (4 types)
│   │   │   ├── UserRoleEnum.java
│   │   │   ├── TransactionTypeEnum.java
│   │   │   ├── BatchStatusEnum.java
│   │   │   └── AuditActionEnum.java
│   │   ├── repositories/                          # JPA repositories (5)
│   │   │   ├── UserRepository.java
│   │   │   ├── ProductRepository.java
│   │   │   ├── BatchRepository.java
│   │   │   ├── TransactionRepository.java
│   │   │   └── AuditLogRepository.java
│   │   ├── services/                              # Business logic (6 interfaces)
│   │   │   ├── ProductService.java
│   │   │   ├── BatchService.java
│   │   │   ├── UserService.java
│   │   │   ├── TransactionService.java
│   │   │   ├── AuditLogService.java
│   │   │   └── imp/                               # Service implementations (5)
│   │   │       ├── ProductServiceImpl.java
│   │   │       ├── BatchServiceImpl.java
│   │   │       ├── UserServiceImpl.java
│   │   │       ├── TransactionServiceImpl.java
│   │   │       └── AuditLogServiceImpl.java
│   │   ├── DTO/                                   # Data Transfer Objects (5)
│   │   │   ├── ProductDTO.java
│   │   │   ├── BatchDTO.java
│   │   │   ├── UserDTO.java
│   │   │   ├── TransactionDTO.java
│   │   │   └── AuditLogDTO.java
│   │   ├── Mappers/                               # MapStruct mappers (4)
│   │   │   ├── ProductMapper.java
│   │   │   ├── BatchMapper.java
│   │   │   ├── UserMapper.java
│   │   │   └── AuditLogMapper.java
│   │   └── payload/
│   │       ├── Request/                           # Request DTOs
│   │       │   ├── ProductRequest.java
│   │       │   ├── BatchRequest.java
│   │       │   ├── TransactionRequest.java
│   │       │   ├── AuditLogFilter.java
│   │       │   ├── LoginRequest.java
│   │       │   └── RegisterRequest.java
│   │       └── Responde/                          # Response wrapper
│   │           └── GeneralResponde.java
│   ├── resources/
│   │   └── application.yaml                       # Spring config
│   └── test/java/com/mstock/api/
│       └── ApiApplicationTests.java               # Basic test
├── docker-compose.yml                             # PostgreSQL 15 container
├── pom.xml                                         # Maven dependencies
├── CAHIER_DES_CHARGES.md                         # Business requirements (338 lines)
├── API_REFERENCE.md                              # API documentation (535 lines)
└── AGENTS.md                                      # Development guide

```

### Key Metrics
- **Total Java Files:** 40+ (entities, controllers, services, repositories, DTOs, mappers)
- **Total Lines of Code:** ~2,500 (excluding test & generated code)
- **Build Tool:** Maven 3.9+ (uses ./mvnw wrapper)
- **Java Version:** 21 (JDK 21)
- **Spring Boot:** 4.0.6

---

## 2. ENTITIES (Database Models)

### User Entity
**Table:** `app_user`  
**Purpose:** System users with roles and audit tracking

| Field | Type | Notes |
|-------|------|-------|
| `id` | Long | Primary key, auto-increment |
| `username` | String | Unique identifier |
| `role` | UserRoleEnum | ADMIN \| PHARMACIAN |
| `email` | String | Email address |
| `password` | String | Hashed password (not yet implemented) |
| `active` | Boolean | Soft delete flag |
| `auditLogs` | List<AuditLog> | One-to-many relationship |
| `transactions` | List<Transaction> | One-to-many relationship |

**Relationships:**
- 1-to-N with AuditLog (cascade delete)
- 1-to-N with Transaction (cascade delete)

---

### Product Entity
**Table:** `products`  
**Purpose:** Medical products catalog with NSN tracking

| Field | Type | Notes |
|-------|------|-------|
| `id` | Long | Primary key, auto-increment |
| `name` | String | Product name (nullable=false) |
| `nsnCode` | String | NSN/Item number (unique, nullable=false) |
| `description` | String | Product description |
| `parLevel` | Integer | Default PAR level for reordering |
| `active` | Boolean | Default: true |
| `version` | Long | Optimistic locking version |
| `batches` | List<Batch> | One-to-many, LAZY loading |
| `transactions` | List<Transaction> | One-to-many, LAZY loading |

**Key Methods:**
- `updateFromRequest(ProductRequest)` - Partial update with null-safety
- Supports partial updates (only non-null/non-empty fields)

---

### Batch Entity
**Table:** `batches` (indexes: lot_number, expiration_date, product_id, status)  
**Purpose:** Track individual lots/batches of products for expiration management

| Field | Type | Notes |
|-------|------|-------|
| `id` | Long | Primary key, auto-increment |
| `lotNumber` | String | Unique lot identifier |
| `quantity` | Integer | Current quantity in stock |
| `expirationDate` | LocalDateTime | Expiration date/time |
| `location` | String | Physical location (Zone/Bin) |
| `status` | BatchStatusEnum | ACTIVE \| QUARANTINE \| RETIRED |
| `createdAt` | Instant | Timestamp (auto-set) |
| `product` | Product | Many-to-one relationship, LAZY |
| `transactions` | List<Transaction> | One-to-many, LAZY loading |
| `version` | Long | Optimistic locking |

**Key Methods:**
- `getDaysUntilExpiration()` - Calculate days to expiration
- `isExpired()` - Check if batch is expired
- `getExpirationAlertLevel()` - Returns: EXPIRED, CRITICAL (<7 days), ATTENTION (7-30 days), NORMAL (>30 days)
- `updateFromRequest(BatchRequest)` - Partial update

**Alert Levels:**
- **CRITICAL:** < 7 days → Immediate action required (email notification)
- **ATTENTION:** 7-30 days → Mark for urgent use
- **NORMAL:** > 30 days → Standard stock

---

### Transaction Entity
**Table:** `transactions` (indexes: batch_id, user_id, created_at, product_id)  
**Purpose:** Immutable audit trail of stock movements

| Field | Type | Notes |
|-------|------|-------|
| `id` | Long | Primary key, auto-increment |
| `type` | TransactionTypeEnum | IN \| OUT \| RETURN |
| `quantity` | Integer | Number of units |
| `reason` | String | Why (e.g., "Patient Use", "Expired", "Recall") |
| `createdAt` | Instant | Auto-captured timestamp |
| `user` | User | Many-to-one, LAZY |
| `product` | Product | Many-to-one, LAZY |
| `batch` | Batch | Many-to-one, LAZY |

**Key Features:**
- **IMMUTABLE AFTER CREATION:** No setters generated (Lombok @Getter only)
- **Read-only after insert:** `createdAt` is `updatable=false`
- Automatically updates Batch quantity:
  - IN: `batch.quantity += quantity`
  - OUT: `batch.quantity -= quantity` (validates stock availability)
  - RETURN: `batch.quantity += quantity`
- Records WHO (user), WHAT (product/batch), WHEN (timestamp), WHY (reason)

---

### AuditLog Entity
**Table:** `audit_logs` (indexes: timestamp, user_id, action, table_name)  
**Purpose:** Append-only immutable compliance trail

| Field | Type | Notes |
|-------|------|-------|
| `id` | Long | Primary key, auto-increment |
| `action` | AuditActionEnum | CREATE \| UPDATE \| DELETE |
| `tableName` | String | Which table changed (batches, transactions, products, app_user) |
| `oldValue` | String | JSON of previous state |
| `newValue` | String | JSON of new state |
| `ipAddress` | String | IP address (from X-Forwarded-For header) |
| `reason` | String | Optional reason (e.g., "FDA Recall") |
| `timestamp` | Instant | Auto-captured (immutable) |
| `user` | User | Many-to-one, LAZY |

**Key Features:**
- **APPEND-ONLY & IMMUTABLE:** No setters generated
- **Never deletable:** Ensure 1-year rolling policy (manual archival)
- Captures all changes automatically via aspect (future enhancement)
- Stores old/new values as JSON for diff analysis
- Indexed on timestamp, user_id, action for fast filtering

---

## 3. ENUMS

### UserRoleEnum
```java
ADMIN       // Full system access, user management
PHARMACIAN  // Inventory operations, viewing
```

**Note:** CAHIER_DES_CHARGES mentions VIEWER, CLERK, OFFICER roles not yet implemented.

---

### TransactionTypeEnum
```java
IN          // Stock receipt (increases batch quantity)
OUT         // Stock withdrawal (decreases batch quantity)
RETURN      // Return to stock (increases batch quantity)
```

---

### BatchStatusEnum
```java
ACTIVE      // Normal stock available for use
QUARANTINE  // Under recall/investigation - blocked from withdrawals
RETIRED     // Expired or removed from service - not available
```

---

### AuditActionEnum
```java
CREATE      // New record created
UPDATE      // Existing record modified
DELETE      // Record deleted (soft delete via status change)
```

---

## 4. REPOSITORIES (JPA Data Access)

All repositories extend `JpaRepository<Entity, Long>` for basic CRUD.

### ProductRepository
**Methods:**
- `findByActiveTrue()` - Get active products
- `findByIdAndActiveTrue(id)` - Get active product by ID
- `existsByNsnCode(nsnCode)` - Check if NSN exists
- `findByIdWithoutRelations(id)` - Avoid N+1 queries
- `findByIdAndActiveTrueWithoutRelations(id)` - Active product without relations

---

### UserRepository
**Methods:**
- `existsByEmail(email)` - Check email uniqueness
- `findByActiveTrue()` - Get active users
- `findByIdAndActiveTrueWithoutRelations(id)` - Specific user
- `findByIdWithoutRelations(id)` - Any user by ID

---

### BatchRepository
**Key Methods:**
- `findByLotNumber(lotNumber)` - Exact lot match
- `existsByLotNumber(lotNumber)` - Duplicate check
- `findByProductId(productId)` - All batches for product
- `findByStatus(status)` - Filter by status (ACTIVE, QUARANTINE, RETIRED)
- `findByExpirationDateBetweenAndStatus(start, end, status)` - Date range queries
- `findByExpirationDateBeforeAndStatus(date, status)` - Expired batches
- `findByIdWithoutRelations(id)` - Avoid N+1
- `findByIdAndStatus(id, status)` - Specific status filter
- `countByProductIdAndStatus(productId, status)` - Batch count
- `searchByLotNumberLike(lotNumber, status)` - Partial match (wildcard)
- `findByProductNsnCodeAndStatus(nsnCode, status)` - Search by product NSN

---

### TransactionRepository
**Key Methods:**
- `findByBatchId(batchId)` - All transactions for batch
- `findByUserId(userId)` - All user transactions
- `findByProductId(productId)` - All transactions for product
- `findByType(type)` - Filter by IN/OUT/RETURN
- `findByCreatedAtBetweenOrderByCreatedAtDesc(start, end)` - Date range
- `findByIdWithoutRelations(id)` - Avoid relationships
- `countByBatchId(batchId)` - Transaction count for batch
- `getProductTransactionHistory(productId)` - Ordered by date DESC
- `getBatchTransactionHistory(batchId)` - Ordered by date DESC

---

### AuditLogRepository
**Key Methods:**
- `findByUserIdOrderByTimestampDesc(userId)` - User audit trail
- `findByActionOrderByTimestampDesc(action)` - Filter by action type
- `findByTableNameOrderByTimestampDesc(tableName)` - Changes to specific table
- `findByTimestampBetweenOrderByTimestampDesc(start, end)` - Date range queries
- `findByReasonOrderByTimestampDesc(reason)` - Filter by reason
- `findByFilterCriteria(userId, action, table, startDate, endDate, pageable)` - Complex search with pagination
- `findOldAuditLogs(cutoffDate)` - For archival (1-year rolling policy)
- `countByUserId(userId)` - User action count
- `countByAction(action)` - Statistics by action type

**Pagination Support:**
- All audit log queries support `Pageable` for pagination
- Default: 20 items per page, page 0 (first page)

---

## 5. CONTROLLERS & API ENDPOINTS

### Base URL: `/api/v0/`

All controllers use `GeneralResponde<T>` wrapper for responses:
```json
{
  "data": {...},
  "msg": "Success message",
  "status": 200
}
```

---

### PRODUCT CONTROLLER (`/api/v0/products`)

| Endpoint | Method | Request | Response | Purpose |
|----------|--------|---------|----------|---------|
| `/` | POST | ProductRequest | ProductDTO | Create product |
| `/` | GET | - | List<ProductDTO> | Get all active products |
| `/{id}` | GET | - | ProductDTO | Get product by ID |
| `/` | PUT | ProductRequest | ProductDTO | Update product (partial) |
| `/{id}` | DELETE | - | Message | Soft delete (set active=false) |

**ProductRequest Fields:**
- `id` (required for update)
- `name` (String)
- `nsnCode` (String, unique)
- `description` (String)
- `parLevel` (Integer)
- `active` (Boolean)

---

### BATCH CONTROLLER (`/api/v0/batches`)

| Endpoint | Method | Request | Response | Purpose |
|----------|--------|---------|----------|---------|
| `/` | POST | BatchRequest | BatchDTO | Create batch |
| `/` | GET | - | List<BatchDTO> | Get all active batches |
| `/{id}` | GET | - | BatchDTO | Get batch by ID |
| `/lot/{lotNumber}` | GET | - | BatchDTO | Get batch by exact lot number |
| `/product/{productId}` | GET | - | List<BatchDTO> | Get batches for product |
| `/alerts/{days}` | GET | - | List<BatchDTO> | Expiration alerts (< N days) |
| `/search` | GET | nsnCode, lotNumber (query) | List<BatchDTO> | Search by NSN or lot (wildcard) |
| `/expired` | GET | - | List<BatchDTO> | Get expired batches |
| `/` | PUT | BatchRequest | BatchDTO | Update batch (partial) |
| `/{id}/quarantine` | PUT | reason (query, optional) | BatchDTO | Mark as QUARANTINE |
| `/{id}` | DELETE | - | Message | Soft delete (set status=RETIRED) |

**BatchRequest Fields:**
- `id` (required for update)
- `lotNumber` (String, unique)
- `quantity` (Integer)
- `expirationDate` (LocalDateTime, ISO 8601)
- `location` (String, e.g., "Zone-A/Bin-1")
- `status` (BatchStatusEnum)
- `productId` (Long, required)

**BatchDTO Response Includes:**
- Product name & NSN code
- Days until expiration (calculated)
- Expiration alert level (NORMAL, ATTENTION, CRITICAL, EXPIRED)

---

### TRANSACTION CONTROLLER (`/api/v0/transactions`)

| Endpoint | Method | Request | Response | Purpose |
|----------|--------|---------|----------|---------|
| `/` | POST | TransactionRequest | TransactionDTO | Record stock movement |
| `/` | GET | - | List<TransactionDTO> | Get all transactions |
| `/{id}` | GET | - | TransactionDTO | Get transaction by ID |
| `/batch/{batchId}` | GET | - | List<TransactionDTO> | Transaction history for batch |
| `/user/{userId}` | GET | - | List<TransactionDTO> | All transactions by user |
| `/history` | GET | productId, startDate, endDate (query) | List<TransactionDTO> | Filtered transaction history |

**TransactionRequest Fields:**
- `type` (TransactionTypeEnum: IN, OUT, RETURN) - REQUIRED
- `quantity` (Integer) - REQUIRED
- `reason` (String) - Optional
- `batchId` (Long) - REQUIRED
- `userId` (Long) - Optional (auto-captured from header if not provided)

**TransactionDTO Response Includes:**
- User name & ID
- Product name & ID
- Batch lot number
- Created timestamp

**Behavior:**
- POST automatically updates Batch quantity
- OUT transactions validate sufficient stock
- Immutable after creation (no updates allowed)

---

### AUDIT LOG CONTROLLER (`/api/v0/audit-logs`)

| Endpoint | Method | Request | Response | Purpose |
|----------|--------|---------|----------|---------|
| `/` | GET | page, size (query) | Page<AuditLogDTO> | Get all audit logs (paginated) |
| `/{id}` | GET | - | AuditLogDTO | Get audit log by ID |
| `/user/{userId}` | GET | - | List<AuditLogDTO> | All actions by user |
| `/table/{tableName}` | GET | - | List<AuditLogDTO> | All changes to table |
| `/search` | POST | AuditLogFilter | Page<AuditLogDTO> | Complex search (paginated) |
| `/report` | GET | startDate, endDate (query) | AuditLogDTO[] | Chain of custody report |
| `/statistics` | GET | - | {creates, updates, deletes, total} | Audit statistics |

**AuditLogFilter Request (POST /search):**
```json
{
  "startDate": "2026-01-01T00:00:00Z",
  "endDate": "2026-04-28T23:59:59Z",
  "userId": 1,
  "action": "UPDATE",
  "tableName": "batches",
  "pageNumber": 0,
  "pageSize": 20
}
```

**All Fields Optional:**
- `startDate` - Filter from date
- `endDate` - Filter to date
- `userId` - Filter by user
- `action` - Filter by CREATE, UPDATE, DELETE
- `tableName` - Filter by table (batches, transactions, products, app_user)
- `pageNumber` - Default 0
- `pageSize` - Default 20

**AuditLogDTO Response Includes:**
- action, tableName, oldValue (JSON), newValue (JSON)
- ipAddress, reason, timestamp, user info

---

### USER CONTROLLER (`/api/v0/users`)

| Endpoint | Method | Request | Response | Purpose |
|----------|--------|---------|----------|---------|
| `/` | POST | RegisterRequest | UserDTO | Create user |
| `/` | GET | - | List<UserDTO> | Get all active users |
| `/{id}` | GET | - | UserDTO | Get user by ID |
| `/` | PUT | RegisterRequest | UserDTO | Update user |
| `/{id}` | DELETE | - | Message | Delete user (soft delete) |

**RegisterRequest Fields:**
- `id` (required for update)
- `username` (String)
- `email` (String)
- `password` (String)
- `role` (UserRoleEnum: ADMIN, PHARMACIAN)

---

## 6. DATABASE SCHEMA

### Database Engine
- **Type:** PostgreSQL 15
- **Container:** Docker (see docker-compose.yml)
- **Connection:** `jdbc:postgresql://localhost:5432/mstock`
- **User:** bakali
- **Password:** bakali
- **DDL:** `create-drop` (recreates schema on app startup - for development)

### Tables & Indexes

#### app_user
```sql
COLUMNS:
  id BIGINT PRIMARY KEY AUTO_INCREMENT
  username VARCHAR(255)
  role ENUM('ADMIN', 'PHARMACIAN')
  email VARCHAR(255)
  password VARCHAR(255)
  active BOOLEAN DEFAULT true
```

#### products
```sql
COLUMNS:
  id BIGINT PRIMARY KEY AUTO_INCREMENT
  name VARCHAR(255) NOT NULL
  nsn_code VARCHAR(255) UNIQUE NOT NULL
  description TEXT
  par_level INT
  active BOOLEAN DEFAULT true
  version BIGINT (optimistic locking)
```

#### batches
```sql
COLUMNS:
  id BIGINT PRIMARY KEY AUTO_INCREMENT
  lot_number VARCHAR(255) UNIQUE NOT NULL
  quantity INT NOT NULL
  expiration_date DATETIME NOT NULL
  location VARCHAR(255) NOT NULL
  status ENUM('ACTIVE', 'QUARANTINE', 'RETIRED') DEFAULT 'ACTIVE'
  product_id BIGINT NOT NULL (FK: products.id)
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  version BIGINT (optimistic locking)

INDEXES:
  idx_lot_number (lot_number)
  idx_expiration_date (expiration_date)
  idx_product_id (product_id)
  idx_status (status)
```

#### transactions
```sql
COLUMNS:
  id BIGINT PRIMARY KEY AUTO_INCREMENT
  type ENUM('IN', 'OUT', 'RETURN') NOT NULL
  quantity INT NOT NULL
  reason VARCHAR(500)
  created_at TIMESTAMP NOT NULL, updatable=false
  user_id BIGINT NOT NULL (FK: app_user.id)
  product_id BIGINT NOT NULL (FK: products.id)
  batch_id BIGINT NOT NULL (FK: batches.id)
  version BIGINT (optimistic locking)

INDEXES:
  idx_batch_id (batch_id)
  idx_user_id (user_id)
  idx_created_at (created_at)
  idx_product_id (product_id)
```

#### audit_logs
```sql
COLUMNS:
  id BIGINT PRIMARY KEY AUTO_INCREMENT
  action ENUM('CREATE', 'UPDATE', 'DELETE') NOT NULL
  table_name VARCHAR(255) NOT NULL
  old_value LONGTEXT
  new_value LONGTEXT
  ip_address VARCHAR(45)
  reason VARCHAR(500)
  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
  user_id BIGINT (FK: app_user.id, nullable)

INDEXES:
  idx_timestamp (timestamp)
  idx_user_id (user_id)
  idx_action (action)
  idx_table_name (table_name)
```

### Database Configuration (application.yaml)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mstock
    username: bakali
    password: bakali
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop         # DEVELOPMENT ONLY
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    show-sql: true
    open-in-view: false
    properties:
      hibernate:
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
server:
  port: 8080
```

**Important Notes:**
- `ddl-auto: create-drop` drops and recreates schema on each startup (development mode)
- For production: change to `validate` or `update` after initial schema creation
- No migrations (Flyway/Liquibase) configured yet - must add before production
- `show-sql: true` logs all SQL (disable in production)
- Batch size 20 for bulk operations
- `open-in-view: false` prevents lazy-loading session issues

---

## 7. AUTHENTICATION & SECURITY

### Current Status
- **Spring Security:** Commented out in pom.xml (not enabled)
- **Authentication:** NOT YET IMPLEMENTED
- **Authorization:** Basic role concept exists (UserRoleEnum) but not enforced

### Planned Implementation (from CAHIER_DES_CHARGES)
- **Method:** CAC (Common Access Card) via proxy header
- **Header:** `X-Forwarded-User` (contains username from CAC)
- **IP Address:** Extracted from `X-Forwarded-For` header
- **Roles (planned):**
  - VIEWER: Read-only access (medical staff)
  - CLERK: Stock operations (receipt, withdrawal)
  - OFFICER: Approvals, audit access
  - ADMIN: System administration

### What's Missing
- Spring Security configuration class
- Custom UserDetailsService implementation
- Authentication filter for CAC header
- Authorization annotations (@PreAuthorize, @RolesAllowed)
- Password hashing/encoding (for UserServiceImpl)
- RBAC (Role-Based Access Control) enforcement

### Security Headers to Implement
- `X-Forwarded-User` → Extract username (CAC authentication)
- `X-Forwarded-For` → Extract IP address for audit trail
- HTTPS requirement (TLS 1.3 recommended)
- CSRF protection (when forms added)
- SQL injection prevention (using JPA, safe from direct SQL)
- XSS prevention (API returns JSON, frontend responsibility)

---

## 8. CURRENT API ROUTES SUMMARY

### All Endpoints (Quick Reference)

**Base URL:** `http://localhost:8080/api/v0`

#### Products (CRUD + Soft Delete)
```
POST   /products                    Create product
GET    /products                    List all active products
GET    /products/{id}               Get product details
PUT    /products                    Update product (partial)
DELETE /products/{id}               Soft delete (active=false)
```

#### Batches (CRUD + Search + Expiration Alerts)
```
POST   /batches                     Create batch/lot
GET    /batches                     List all active batches
GET    /batches/{id}                Get batch details
GET    /batches/lot/{lotNumber}     Find by lot number (exact)
GET    /batches/product/{productId} Get batches for product
GET    /batches/alerts/{days}       Expiration alerts (< N days)
GET    /batches/search?nsnCode=X&lotNumber=Y   Search batches
GET    /batches/expired             Get expired batches
PUT    /batches                     Update batch (partial)
PUT    /batches/{id}/quarantine?reason=...     Quarantine batch
DELETE /batches/{id}                Soft delete (RETIRED status)
```

#### Transactions (Record Stock Movements - Immutable)
```
POST   /transactions                Record transaction (IN|OUT|RETURN)
GET    /transactions                Get all transactions
GET    /transactions/{id}           Get transaction
GET    /transactions/batch/{batchId}        Get batch transaction history
GET    /transactions/user/{userId}         Get user transactions
GET    /transactions/history?productId=X&startDate=Y&endDate=Z   History
```

#### Audit Logs (Read-Only Compliance Trail)
```
GET    /audit-logs?page=0&size=20   Get paginated audit logs
GET    /audit-logs/{id}             Get single audit log
GET    /audit-logs/user/{userId}    Get user audit trail
GET    /audit-logs/table/{tableName}        Get table change history
POST   /audit-logs/search           Search with complex filter
GET    /audit-logs/report?startDate=X&endDate=Y   Chain of custody
GET    /audit-logs/statistics       Get audit statistics
```

#### Users (CRUD)
```
POST   /users                       Create user
GET    /users                       List all active users
GET    /users/{id}                  Get user details
PUT    /users                       Update user
DELETE /users/{id}                  Soft delete (active=false)
```

---

## 9. WHAT THE API CURRENTLY DOES

### Core Functionality Implemented

1. **Batch/Lot Tracking**
   - Create batches with lot numbers, quantities, expiration dates
   - Track physical location (Zone/Bin)
   - Automatic expiration alert calculation (CRITICAL < 7 days, ATTENTION 7-30 days)
   - Search by NSN code or lot number (wildcard)
   - Quarantine batches for recalls

2. **Stock Movement Recording**
   - Record IN (receipt), OUT (withdrawal), RETURN transactions
   - Automatic batch quantity updates
   - Validation of sufficient stock before OUT transactions
   - Immutable transaction records (no edits after creation)

3. **Audit Trail & Compliance**
   - Append-only audit log for all entity changes
   - Captures WHO (user), WHAT (table/action), WHEN (timestamp), WHERE (IP)
   - Stores old/new values as JSON for change analysis
   - Complex search with date range, user, action, table filters
   - Pagination support (20 items/page)
   - Chain of custody reports

4. **Product Catalog**
   - Create/update/list medical products
   - NSN code tracking (unique identifier)
   - PAR level default settings
   - Active/inactive status

5. **User Management** (Basic)
   - Create/update/list users
   - Role assignment (ADMIN, PHARMACIAN)
   - Soft delete (active flag)

### Limitations & Not Yet Implemented

- **No Authentication:** CAC header not parsed, no user context enforced
- **No Authorization:** No role-based access control
- **No Dashboard:** No aggregated metrics or widgets
- **No Reporting:** No PDF/CSV export (structure ready)
- **No Notifications:** No email alerts on critical expiration
- **No Barcode Scanning:** No integration with barcode readers
- **No PAR Level Management:** Endpoints not created for per-location PAR configuration
- **No Cold Chain Tracking:** Temperature/humidity monitoring (Phase 2)
- **No Frontend:** Angular UI not started

---

## 10. WHAT A FRONTEND NEEDS TO CALL

### Typical User Workflows

#### Workflow 1: Stock Receipt
1. POST `/api/v0/products` → Create product (if new) → Get `productId`
2. POST `/api/v0/batches` → Create batch with `productId`, `lotNumber`, `quantity`, `expirationDate`, `location`
3. POST `/api/v0/transactions` → Record IN transaction with `batchId`, `quantity`, `reason="Supplier Delivery"`
4. GET `/api/v0/audit-logs/table/batches` → Verify audit trail

#### Workflow 2: Stock Withdrawal (Patient Use)
1. GET `/api/v0/batches/product/{productId}` → Show available batches
2. GET `/api/v0/batches/{id}` → Show batch details, expiration status
3. POST `/api/v0/transactions` → Record OUT transaction with `batchId`, `quantity`, `reason="Patient Use"`
4. GET `/api/v0/batches/{id}` → Verify quantity updated

#### Workflow 3: Expiration Monitoring
1. GET `/api/v0/batches/alerts/30` → Show batches expiring in 30 days
2. Filter results by alert level (CRITICAL, ATTENTION, NORMAL)
3. PUT `/api/v0/batches/{id}/quarantine?reason=Approaching+Expiration` → Move CRITICAL items to quarantine
4. GET `/api/v0/transactions/batch/{batchId}` → Show full batch history

#### Workflow 4: Recall Response (FDA/EMA Recall)
1. GET `/api/v0/batches/search?nsnCode=NSN-123456` → Find all batches for product
2. PUT `/api/v0/batches/{id}/quarantine?reason=FDA+Recall` → Quarantine each batch
3. GET `/api/v0/audit-logs/report?startDate=...&endDate=...` → Generate chain of custody
4. Export/print for regulatory compliance

#### Workflow 5: Audit Trail Review (Military Compliance)
1. POST `/api/v0/audit-logs/search` → Search with filters (date range, user, action, table)
2. GET `/api/v0/audit-logs/user/{userId}` → Show all actions by user
3. GET `/api/v0/audit-logs/statistics` → Dashboard summary stats
4. GET `/api/v0/audit-logs/report` → Generate compliance report

### Required Frontend Features

1. **Dashboard**
   - Expiration alert widget (count by severity)
   - PAR level status
   - Recent activity feed

2. **Batch/Product Search**
   - Search bar (NSN, lot number, product name)
   - Results table with sorting/filtering
   - Quick actions (view details, quarantine, history)

3. **Stock Withdrawal Form**
   - Barcode scanner input (future: USB keyboard simulation)
   - Quantity picker
   - Reason dropdown
   - Confirmation dialog

4. **Batch Details View**
   - Batch info (lot, quantity, expiration, location, status)
   - Days until expiration (with alert color)
   - Transaction history (IN/OUT/RETURN with timestamps)
   - Edit batch (update quantity, location)
   - Quarantine action

5. **Audit Log Viewer**
   - Filterable table (date, user, action, table)
   - Old/new value diff view
   - Export to PDF/CSV

6. **Reports**
   - Expiration forecast (by date range)
   - Chain of custody (for recalls)
   - User activity log
   - Batch movement history

### API Response Format

All endpoints return:
```json
{
  "data": {...},
  "msg": "Operation successful",
  "status": 200
}
```

**Error Response (4xx/5xx):**
```json
{
  "data": null,
  "msg": "Error description",
  "status": 400
}
```

---

## 11. DEVELOPMENT STATUS & NEXT STEPS

### What's Done
- ✅ Entities (User, Product, Batch, Transaction, AuditLog)
- ✅ Enums (UserRoleEnum, TransactionTypeEnum, BatchStatusEnum, AuditActionEnum)
- ✅ Repositories (all JPA repos with custom queries)
- ✅ Controllers (REST endpoints mapped)
- ✅ Services (interfaces + partial implementations)
- ✅ DTOs & Mappers (MapStruct configured)
- ✅ Database schema (PostgreSQL 15, indexes)
- ✅ Request/Response payloads

### What's In Progress
- 🔄 Service implementations (BatchServiceImpl, TransactionServiceImpl, etc.)
- 🔄 Business logic (stock validation, quantity updates)
- 🔄 Integration testing

### What's Next (Priority Order)
1. **Complete Service Implementations**
   - Implement business logic in ServiceImpl classes
   - Add transaction validation (prevent negative stock)
   - Add audit log creation (via AOP aspect)

2. **Authentication & Authorization**
   - Enable Spring Security
   - Implement CAC header parsing (X-Forwarded-User)
   - Add RBAC enforcement

3. **Testing**
   - Unit tests for services
   - Integration tests for repositories
   - End-to-end tests for controllers

4. **Frontend (Angular 21)**
   - Dashboard with widgets
   - Product/batch search
   - Withdrawal form with barcode input
   - Audit log viewer

5. **Production Readiness**
   - Database migration strategy (Flyway)
   - Performance tuning (indexes, connection pooling)
   - Deployment configuration
   - Documentation

---

## 12. DEPENDENCIES & BUILD INFO

### Maven Build
```bash
./mvnw clean install              # Full build
./mvnw spring-boot:run           # Run app
./mvnw test                       # Run tests
./mvnw compile                    # Compile only
```

### Key Dependencies
- **Spring Boot 4.0.6** (spring-boot-starter-parent)
- **Spring Data JPA** (Hibernate ORM)
- **PostgreSQL 15 Driver** (org.postgresql:postgresql)
- **Lombok** (boilerplate reduction)
- **MapStruct 1.6.3** (DTO mapping)
- **Validation API** (spring-boot-starter-validation)
- **WebClient** (reactive HTTP client)

### Database
```bash
docker-compose up                 # Start PostgreSQL 15 container
docker-compose down               # Stop container
```

**Container Details:**
- Image: postgres:15
- Port: 5432
- Database: mstock
- User: bakali / Password: bakali
- Volume: ./volumes/postgres-data

---

## SUMMARY

The **M-Stock API** is a **military hospital stock management system** designed for tracking medical supplies, batches, and compliance auditing. 

**Current State:**
- Core entities and repositories implemented
- Basic CRUD controllers in place
- PostgreSQL database configured
- RESTful API structure ready
- 8-week MVP timeline in CAHIER_DES_CHARGES

**Frontend Needed:**
- Dashboard with expiration alerts
- Batch search & detail views
- Stock withdrawal form
- Audit log viewer
- Reporting pages

**Key Features:**
- Expiration forecasting (critical < 7 days)
- Immutable audit trail (compliance)
- Soft deletes (RETIRED status)
- Batch quarantine (recall response)
- Transaction history tracking
- Optimistic locking (concurrent updates)

**To Run Locally:**
```bash
docker-compose up                 # Start DB
./mvnw clean install              # Build
./mvnw spring-boot:run           # Start API (port 8080)
# API ready at http://localhost:8080/api/v0
```

