# DAO Architecture Explanation

## Overview

This application uses the **DAO (Data Access Object) Pattern** to separate database operations from business logic. The DAO pattern provides a clean abstraction layer between the application code and the database.

## How DAO Works

### 1. **DAO Pattern Structure**

The DAO pattern consists of two main components:

- **DAO Interface** (`*DAO.java`): Defines the contract (methods) for database operations
- **DAO Implementation** (`*DAOImpl.java`): Contains the actual SQL queries and database logic

### 2. **Key Components**

#### **DatabaseConnection** (`util/DatabaseConnection.java`)
- Centralized connection management
- Provides a single shared connection to MySQL database
- Handles connection creation, reuse, and cleanup
- All DAOs use `DatabaseConnection.getConnection()` to access the database

#### **Model Classes** (`model/*.java`)
- Plain Java objects representing database tables
- Contain getters/setters for each field
- Examples: `Victim`, `Administrator`, `IncidentReport`, `Perpetrator`

#### **DAO Interfaces** (`dao/*DAO.java`)
- Define method signatures for CRUD operations
- Examples: `findById()`, `create()`, `update()`, `delete()`, `findAll()`
- Each entity has its own DAO interface

#### **DAO Implementations** (`dao/*DAOImpl.java`)
- Implement the interface methods
- Execute SQL queries using `PreparedStatement` (prevents SQL injection)
- Map database `ResultSet` rows to Model objects
- Handle exceptions and connection cleanup using try-with-resources

### 3. **Typical DAO Operation Flow**

```
1. Controller/Service calls DAO method
   ↓
2. DAO gets connection from DatabaseConnection
   ↓
3. DAO creates PreparedStatement with SQL query
   ↓
4. DAO executes query (SELECT/INSERT/UPDATE/DELETE)
   ↓
5. DAO maps ResultSet to Model object (if SELECT)
   ↓
6. DAO returns Model object or boolean (success/failure)
   ↓
7. Connection/resources automatically closed (try-with-resources)
```

## File Interactions

### **Architecture Layers**

```
┌─────────────────────────────────────┐
│         Controllers (UI)            │  ← JavaFX Controllers handle user input
└──────────────┬──────────────────────┘
               │
               ↓
┌─────────────────────────────────────┐
│         Services (Business Logic)   │  ← Handle validation, authentication, etc.
└──────────────┬──────────────────────┘
               │
               ↓
┌─────────────────────────────────────┐
│         DAO Layer (Data Access)     │  ← Database operations
└──────────────┬──────────────────────┘
               │
               ↓
┌─────────────────────────────────────┐
│      DatabaseConnection (Util)      │  ← Connection management
└──────────────┬──────────────────────┘
               │
               ↓
┌─────────────────────────────────────┐
│         MySQL Database              │  ← Actual database
└─────────────────────────────────────┘
```

### **How Files Interact**

#### **1. Controllers → DAOs**
- Controllers directly instantiate DAO implementations
- Example: `ReportIncidentController` creates `PerpetratorDAOImpl`, `IncidentReportDAOImpl`
- Controllers call DAO methods to fetch/save data based on user actions

**Example:**
```java
// In ReportIncidentController.java
private final PerpetratorDAO perpDAO = new PerpetratorDAOImpl();
private final IncidentReportDAO incidentDAO = new IncidentReportDAOImpl();

// Later in code:
Perpetrator perp = perpDAO.findByIdentifier(identifier);
incidentDAO.create(newReport);
```

#### **2. Services → DAOs**
- Services use DAOs for database operations
- Services add business logic (validation, security, etc.) before/after DAO calls
- Example: `VictimAuthenticationService` uses `VictimDAO` for login/registration

**Example:**
```java
// In VictimAuthenticationService.java
private final VictimDAO victimDAO = new VictimDAOImpl();

public boolean register(String name, String email, String password) {
    // Business logic: validation
    if (!ValidationUtils.isValidEmail(email)) return false;
    
    // Check for duplicates using DAO
    Victim existing = victimDAO.findByEmail(email);
    if (existing != null) return false;
    
    // Hash password, create object, save via DAO
    String hash = SecurityUtils.hashPassword(password);
    Victim newVictim = new Victim(name, email, hash);
    return victimDAO.create(newVictim);
}
```

#### **3. DAOs → DatabaseConnection**
- All DAOs use `DatabaseConnection.getConnection()` to get database connection
- No DAO creates its own connection - centralized management

**Example:**
```java
// In VictimDAOImpl.java
public Victim findByEmail(String email) throws SQLException {
    String sql = "SELECT * FROM Victims WHERE ContactEmail = ?";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, email);
        // ... execute query and map results
    }
}
```

#### **4. DAOs → Models**
- DAOs convert database rows (`ResultSet`) into Model objects
- Each DAO has a private `mapResultSetTo*()` method for this conversion

**Example:**
```java
// In VictimDAOImpl.java
private Victim mapResultSetToVictim(ResultSet rs) throws SQLException {
    Victim victim = new Victim();
    victim.setVictimID(rs.getInt("VictimID"));
    victim.setName(rs.getString("Name"));
    victim.setContactEmail(rs.getString("ContactEmail"));
    // ... map all fields
    return victim;
}
```

### **Complete Interaction Example: User Login**

```
1. User enters email/password in LoginController
   ↓
2. LoginController calls VictimAuthenticationService.login()
   ↓
3. Service validates input format
   ↓
4. Service calls VictimDAO.findByEmail(email)
   ↓
5. DAO gets connection from DatabaseConnection
   ↓
6. DAO executes SQL: SELECT * FROM Victims WHERE ContactEmail = ?
   ↓
7. DAO maps ResultSet to Victim model object
   ↓
8. DAO returns Victim object to Service
   ↓
9. Service verifies password hash using SecurityUtils
   ↓
10. Service returns Victim object to Controller
    ↓
11. Controller navigates to dashboard if login successful
```

## DAO Files in This Project

### **Core Entity DAOs**
- `VictimDAO` / `VictimDAOImpl` - Victim account management
- `AdministratorDAO` / `AdministratorDAOImpl` - Admin account management
- `PerpetratorDAO` / `PerpetratorDAOImpl` - Perpetrator records
- `IncidentReportDAO` / `IncidentReportDAOImpl` - Incident reports
- `EvidenceDAO` / `EvidenceDAOImpl` - Evidence files

### **Supporting DAOs**
- `AttackTypeDAO` / `AttackTypeDAOImpl` - Attack type lookup
- `ThreatLevelLogDAO` / `ThreatLevelLogDAOImpl` - Threat level history
- `VictimStatusLogDAO` / `VictimStatusLogDAOImpl` - Victim status history
- `RecycleBinDAO` / `RecycleBinDAOImpl` - Soft delete recovery
- `ReportEvaluationNotesDAO` / `ReportEvaluationNotesDAOImpl` - Admin notes

## Key Benefits of This Architecture

1. **Separation of Concerns**: Database logic isolated from business logic
2. **Testability**: Can mock DAOs for testing controllers/services
3. **Maintainability**: SQL changes only affect DAO implementations
4. **Security**: All queries use PreparedStatement (prevents SQL injection)
5. **Reusability**: Same DAO can be used by multiple controllers/services
6. **Consistency**: Standardized pattern across all entities

## Common Patterns Used

- **Try-with-resources**: Automatic connection/statement cleanup
- **PreparedStatement**: Parameterized queries for security
- **ResultSet Mapping**: Convert database rows to Java objects
- **Exception Handling**: SQLException propagated to caller
- **Singleton Connection**: Shared connection via DatabaseConnection

