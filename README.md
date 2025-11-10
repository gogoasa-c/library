# Library Management System

A comprehensive library management application demonstrating **Hexagonal Architecture** (Ports and Adapters) principles with clear separation of concerns and a modular design.

---

## 🏗️ Architectural Overview

This project exemplifies hexagonal architecture by organizing code into distinct layers with well-defined boundaries. The core domain logic remains isolated from external concerns, making the system maintainable, testable, and adaptable to change.

```
┌─────────────────────────────────────────────────────────────────┐
│                         ADAPTERS (Input)                         │
│                      Infrastructure Layer                        │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────────┐   │
│  │ CLI/TUI      │  │ Menu Systems │  │ Report Viewer      │   │
│  │ Input Parser │  │ (Book, Auth, │  │ (Swing GUI)        │   │
│  │              │  │  Collection) │  │                    │   │
│  └──────┬───────┘  └──────┬───────┘  └──────────┬─────────┘   │
│         │                 │                      │              │
│         └─────────────────┼──────────────────────┘              │
│                           ▼                                     │
├═══════════════════════════════════════════════════════════════════┤
│                      APPLICATION LAYER                          │
│                     (Ports & Services)                          │
│                                                                 │
│  ┌────────────────────────────────────────────────────────┐   │
│  │               INPUT PORTS (Interfaces)                  │   │
│  │  • BookManagementInput                                  │   │
│  │  • AuthorManagementInput                                │   │
│  │  • CollectionManagementInput                            │   │
│  │  • ReportingInput                                       │   │
│  └────────────────────┬───────────────────────────────────┘   │
│                       ▼                                         │
│  ┌────────────────────────────────────────────────────────┐   │
│  │           APPLICATION SERVICES                          │   │
│  │  • BookManagementService                                │   │
│  │  • AuthorManagementService                              │   │
│  │  • CollectionManagementService                          │   │
│  │  • ReportingService                                     │   │
│  └────────────────────┬───────────────────────────────────┘   │
│                       ▼                                         │
│  ┌────────────────────────────────────────────────────────┐   │
│  │              OUTPUT PORTS (Interfaces)                  │   │
│  │  • BookPersistence                                      │   │
│  │  • AuthorPersistence                                    │   │
│  │  • CollectionPersistence                                │   │
│  └────────────────────────────────────────────────────────┘   │
│                       │                                         │
├═══════════════════════════════════════════════════════════════════┤
│                         DOMAIN LAYER                            │
│                     (Business Logic)                            │
│                                                                 │
│  ┌────────────────────────────────────────────────────────┐   │
│  │                 DOMAIN ENTITIES                         │   │
│  │  • Book (with borrow() behavior)                        │   │
│  │  • Author                                               │   │
│  │  • Collection                                           │   │
│  └────────────────────────────────────────────────────────┘   │
├═══════════════════════════════════════════════════════════════════┤
│                      ADAPTERS (Output)                          │
│                   Infrastructure Layer                          │
│                                                                 │
│  ┌────────────────────────────────────────────────────────┐   │
│  │          FILE-BASED REPOSITORIES                        │   │
│  │  • BookRepository                                       │   │
│  │  • AuthorRepository                                     │   │
│  │  • CollectionRepository                                 │   │
│  │                                                         │   │
│  │  (All extend AbstractFileRepository)                    │   │
│  └────────────────────────────────────────────────────────┘   │
│                           │                                     │
│                           ▼                                     │
│                    JSON Persistence                             │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🎯 Separation of Concerns

### **1. Domain Layer** (`domain.entity`)
The heart of the application, containing pure business logic with zero dependencies on infrastructure or frameworks.

**Entities:**
- **`Book`**: Immutable record with intrinsic domain behavior (e.g., `borrow()` method)
- **`Author`**: Simple value object representing author identity
- **`Collection`**: Categorization entity for grouping books

**Characteristics:**
- Immutable by design (Java records)
- Self-contained business rules
- No framework dependencies
- Domain behavior encapsulated within entities

### **2. Application Layer** (`application`)
Orchestrates use cases by coordinating between domain entities and infrastructure adapters.

#### **Input Ports** (`application.input`)
Interfaces defining what the application can do:
- `BookManagementInput`: Book lifecycle operations (add, retrieve, borrow)
- `AuthorManagementInput`: Author management
- `CollectionManagementInput`: Collection management
- `ReportingInput`: Generate reports

#### **Application Services** (`application.service`)
Concrete implementations of input ports that:
- Validate business rules (e.g., author/collection existence before creating books)
- Orchestrate domain operations
- Coordinate with output ports
- Transform DTOs to domain entities and vice versa

#### **Output Ports** (`application.output`)
Interfaces defining persistence contracts:
- `BookPersistence`
- `AuthorPersistence`
- `CollectionPersistence`

#### **DTOs** (`application.dto`)
Data transfer objects for cross-layer communication:
- `AddBookCommand`, `AddAuthorCommand`, `AddCollectionCommand`
- `BookReport`, `CollectionReport`

### **3. Infrastructure Layer** (`infra`)
Implements technical concerns and adapters for external systems.

#### **Input Adapters** (`infra.input`)
Handle user interaction:
- **`CLIInputParser`**: Terminal UI orchestrator, delegates to menu systems
- **Menu systems**: Focused menu handlers (AuthorMenu, BookMenu, CollectionMenu)
- **`ReportViewerSwing`**: GUI adapter for report visualization

#### **Output Adapters** (`infra.output`)
Implement persistence:
- **`AbstractFileRepository<T>`**: Generic file-based persistence with JSON serialization
  - ID generation
  - CRUD operations
  - Java Time support (LocalDate serialization)
- **Concrete repositories**: BookRepository, AuthorRepository, CollectionRepository

### **4. Configuration Layer** (`config`)
**`DependencyOrchestrator`**: Wires dependencies and manages object lifecycle
- Creates output adapters (repositories)
- Instantiates application services
- Builds input adapters (CLI parser, menus)
- Follows Dependency Inversion Principle (depends on abstractions)

---

## 🔄 Generic Systems & Reusability

### **1. Abstract File Repository**
A generic, reusable persistence mechanism (`AbstractFileRepository<T>`) that:
- Provides type-safe JSON serialization/deserialization
- Handles automatic ID generation
- Supports CRUD operations for any entity type
- Configurable via TypeReference and ID extractor function
- Includes Java Time module for modern date/time handling

**Benefits:**
- Eliminates code duplication across repositories
- Consistent persistence behavior
- Easy to extend for new entities
- Testable in isolation

### **2. Menu System**
Modular menu architecture with:
- **`MenuItem`**: Encapsulates menu options with display text and actions
- **`MenuHandler`**: Generic menu rendering and navigation logic
- Specialized menus (AuthorMenu, BookMenu, CollectionMenu) focus on domain-specific operations

**Benefits:**
- Separation between presentation logic and business logic
- Reusable menu infrastructure
- Easy to add new menu flows

### **3. DTOs and Command Pattern**
Standardized data transfer using command objects:
- Decouple input parsing from domain operations
- Provide validation points
- Enable easy transformation between layers

---

## 🔌 Hexagonal Architecture in Action

### **Ports**
- **Input Ports**: Define use cases (what the application does)
- **Output Ports**: Define persistence contracts (what the application needs)

### **Adapters**
- **Input Adapters**: CLI, Menus, GUI Viewers (could be REST API, GraphQL, etc.)
- **Output Adapters**: File repositories (could be SQL, NoSQL, REST clients, etc.)

### **Key Benefits Demonstrated**

1. **Testability**: Application layer can be tested without any infrastructure
   - Input ports can be driven by test doubles
   - Output ports can be mocked/stubbed

2. **Flexibility**: Infrastructure is pluggable
   - Swap CLI for REST API without changing application logic
   - Replace file storage with database without touching business rules

3. **Independence**: Domain logic is framework-agnostic
   - No Jackson annotations in domain entities
   - No UI concerns in business logic

4. **Clarity**: Clear boundaries and responsibilities
   - Each layer has a single, well-defined purpose
   - Dependencies point inward (toward domain)

---

## 🌟 Design Patterns Applied

- **Hexagonal Architecture (Ports & Adapters)**: Overall structure
- **Dependency Inversion**: High-level modules don't depend on low-level modules
- **Repository Pattern**: Abstraction over data persistence
- **Service Layer Pattern**: Application services orchestrate use cases
- **Command Pattern**: DTOs encapsulate operations with their data
- **Template Method**: AbstractFileRepository defines skeleton, subclasses provide specifics
- **Immutability**: Domain entities are immutable records

---

## 📦 Technology Stack

- **Java 21**: Records, pattern matching, modern language features
- **Jackson**: JSON serialization with Java Time support
- **JUnit 5**: Testing framework
- **Maven**: Build and dependency management
- **Swing**: Optional GUI for report viewing

---

## 💡 Key Takeaways

This project demonstrates:

1. **Clean boundaries** between domain, application, and infrastructure
2. **Dependency direction** always points toward the domain core
3. **Substitutability** of adapters without affecting business logic
4. **Testability** through interface-based design
5. **Scalability** via modular, loosely-coupled architecture
6. **Generic systems** that promote code reuse and consistency

The architecture makes the system resilient to change: new features can be added by implementing existing ports, and infrastructure can be swapped by providing new adapters—all without modifying the core domain logic.
