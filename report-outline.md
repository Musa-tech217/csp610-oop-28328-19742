# SmartLib Report Outline

This outline is intended as a starting structure for the final PDF report. It is written as a planning document, not as a finished submission.

## Abstract

Summarize the purpose of SmartLib, the technologies used, and the main outcomes of the work. Keep this short and focused on the final architecture.

## 1. Introduction

- Briefly explain the SmartLib problem domain.
- State that the project was developed incrementally.
- Introduce the goals of the assignment: clean OOP design, patterns, generics, functional style, concurrency, and modern Java.

## 2. Domain Model and SOLID Refactoring

- Describe the core entities: `Book`, `Member`, `Loan`, `Reservation`, `Fine`, and `Notification`.
- Explain the move away from a single manager-style class.
- Discuss how each SOLID principle appears in the final design.

## 3. Design Patterns

### 3.1 Builder

- Explain why `LoanBuilder` is useful for required and optional loan data.

### 3.2 Decorator

- Explain the notification pipeline and why composition works better than subclass combinations here.

### 3.3 Observer

- Explain event publication, listeners, and the benefit of decoupled reactions.

## 4. Generics and Collections

- Discuss the design of `Catalogue<T extends Borrowable & Comparable<T>>`.
- Explain wildcard choices and PECS.
- Cover type erasure, raw types, and the type-token example.

## 5. Functional Programming

- Describe the analytics pipelines in `LibraryAnalytics`.
- Explain the custom collector and its accumulation model.
- Mention method references and function composition.

## 6. Concurrency and Thread Safety

- Explain the `BookReturnProcessor` workflow.
- Explain why `CompletableFuture` was chosen.
- Discuss `InventoryManager`, read/write locks, and conditions.
- Include short discussion of race conditions, deadlocks, and livelocks.

## 7. Modern Java Features

- Explain sealed result modeling with `LoanResult`.
- Discuss records, pattern matching, switch expressions, text blocks, and JPMS.

## 8. Testing and Coverage

- Summarize the automated tests across the project.
- Mention Mockito where side effects were verified.
- Refer to JaCoCo HTML and XML reports.

## 9. Lessons Learned

- Reflect on what changed as the design matured.
- Note trade-offs between simplicity, extensibility, and readability.
- Mention how testing supported refactoring.

## 10. Conclusion

- Summarize what the final system demonstrates.
- Briefly mention possible next steps such as database persistence, REST APIs, or a GUI.

## 11. References

Use only real, verified references in the final PDF.

Possible sources to include:

- Official Java 17 documentation
- Maven documentation
- JUnit 5 documentation
- Mockito documentation
- JaCoCo documentation
- PlantUML documentation
- Course notes or the approved design-pattern reference used for the assignment

Do not invent citations or bibliographic details.
