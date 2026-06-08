# CSP610-OOP
## Developed By
- Musa Alie Zain ID 28328
- Zainab Sesay ID 19742


# SmartLib

SmartLib is a Java 17 Maven project built around a small library-management domain. It started as an object-oriented programming assignment and was extended step by step to cover core design ideas such as SOLID refactoring, GoF patterns, generics, streams, concurrency, and modern Java features.

## What the project includes

- core domain types for books, members, loans, reservations, fines, and notifications
- borrowing policies for standard, premium, and student members
- in-memory repositories for isolated testing
- Builder, Decorator, and Observer pattern examples
- generic catalogue and reflection/type-token examples
- stream analytics and a custom collector
- concurrent return processing and a lock-based inventory manager
- sealed result modeling, pattern matching, switch expressions, text blocks, and JPMS

## Package layout

```text
src/main/java
|- module-info.java
`- smartlib
   |- concurrent
   |- domain
   |- functional
   |- generics
   |- modern
   `- patterns
      |- behavioural
      |- creational
      `- structural

src/test/java
`- smartlib
   |- concurrent
   |- domain
   |- functional
   |- generics
   |- modern
   `- patterns

uml/
```

## Architecture summary

The codebase is centered on the `smartlib.domain` package. Domain rules are expressed through small interfaces and immutable records where that makes sense. Service classes handle orchestration, repositories isolate persistence, and policies keep borrowing rules separate from workflow logic.

Other packages are grouped by concept:

- `smartlib.patterns.*` contains the pattern-focused examples
- `smartlib.generics` contains reusable type-safe utilities and demonstrations
- `smartlib.functional` contains analytics-style stream pipelines
- `smartlib.concurrent` contains thread-safe workflow and inventory code
- `smartlib.modern` contains Java 17+ language feature examples

## Patterns and Java features used

- Builder: `LoanBuilder`
- Decorator: notification wrappers for logging, retry, and rate limiting
- Observer: `LibraryEventBus` and event listeners
- Generics: bounded type parameters, wildcards, PECS, safe casting, type tokens
- Functional style: streams, collectors, method references, predicate/function composition
- Concurrency: `CompletableFuture`, `ExecutorService`, `AtomicInteger`, `ReentrantReadWriteLock`, `Condition`
- Modern Java: records, sealed interfaces, pattern matching, switch expressions, text blocks, JPMS

## UML and report assets

PlantUML source files are in `uml/`.

The report outline is in:

- `report-outline.md`

## Build and test

Requirements:

- Java 17
- Maven 3.9+

Run the full test suite:

```bash
mvn clean test
```

Generate JaCoCo coverage output:

```bash
mvn clean test jacoco:report
```

Coverage reports are written to:

- `target/site/jacoco/index.html`
- `target/site/jacoco/jacoco.xml`

## Notes

The project uses Java 17 preview support for pattern-matching `switch`, so the Maven configuration already passes `--enable-preview` for compile and test runs.
