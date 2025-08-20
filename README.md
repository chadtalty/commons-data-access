# Commons Data Access

Tiny, focused helpers for building **type-safe, JSON-driven JPA queries**.
This repo pairs two modules:

- **`commons-data-query`** – DTOs and enums describing filters, joins, and sorting.
- **`commons-data-access`** – Spring Data JPA bridge: converts those DTOs into `Specification<T>` and provides a base DAO.

Use them together to accept JSON criteria, validate it, and run efficient database queries with **zero query string concatenation**.

---

## Features

- Polymorphic filter model (`basic`, `contains`, `between`, `datetime`) with Jackson.
- Jakarta Validation on all inputs.
- Handlers for each filter that generate JPA `Specification<T>` with correct typing/casting.
- Optional joins and sorted pagination via `PageableCriteria`.
- Java 21 ready. Minimal dependencies.

---

## Install

Add both modules to your app (versions are examples):

```xml
<!-- commons-data-query -->
<dependency>
  <groupId>com.chadtalty</groupId>
  <artifactId>commons-data-query</artifactId>
  <version>0.0.1</version>
</dependency>

<!-- commons-data-access -->
<dependency>
  <groupId>com.chadtalty</groupId>
  <artifactId>commons-data-access</artifactId>
  <version>0.0.1</version>
</dependency>
```

Your app should already include Spring Data JPA and a JPA provider (Hibernate), plus Jackson.

---

## Key types (query DTOs)

From `commons-data-query`:

- `Filter` (polymorphic base)
  - `BasicFilter` – `EQUAL`, `NOT_EQUAL`, `GREATER_THAN`, `LESS_THAN`, `GREATER_THAN_OR_EQUAL`, `LESS_THAN_OR_EQUAL`
  - `ContainsFilter` – `IN`
  - `BetweenFilter` – `BETWEEN` with `Instant startDateTime` / `Instant endDateTime`
  - `DateTimeFilter` – `AFTER`, `AFTER_OR_EQUAL`, `BEFORE`, `BEFORE_OR_EQUAL`, `EQUAL`, `NOT_EQUAL` with `Instant value`
- `Criteria` – `List<JoinSpec> joins`, `List<Filter> filters`, `SortSpec sort`
- `PageableCriteria` – extends `Criteria` with `page` and `size`
- `JoinSpec` – `{ join: "relation.path", filter: BasicFilter(...) }`
- `SortSpec` – `{ ascending: [..], descending: [..] }`

> **Note on `field` names**: the `field` inside filters is the **JPA attribute path** (e.g., `"createdAt"`, `"customer.address.postalCode"`), not a DB column name.

---

## Quick start

### 1) Define a repository and DAO

```java
// Your entity repository
public interface OrderRepository extends EntityRepository<Order, Long> {}

// Minimal DAO (uses the base implementation)
@Repository
public class OrderDAO extends AbstractDAO<Order, OrderRepository> {}
```

### 2) Build criteria in code

```java
var createdBetween = BetweenFilter.builder()
    .field("createdAt")
    .startDateTime(Instant.parse("2025-08-01T00:00:00Z"))
    .endDateTime(Instant.parse("2025-08-31T23:59:59Z"))
    .build();

var statusIn = ContainsFilter.builder()
    .field("status")
    .values(List.of("OPEN", "PENDING"))
    .build();

var newerThan = DateTimeFilter.builder()
    .field("updatedAt")
    .operator(DateTimeFilter.Operator.AFTER)
    .value(Instant.parse("2025-08-15T00:00:00Z"))
    .build();

var criteria = new Criteria();
criteria.setFilters(List.of(createdBetween, statusIn, newerThan));
criteria.setSort(new SortSpec(List.of("createdAt"), List.of("totalAmount"))); // ASC then DESC

// Pagination
var pageable = new PageableCriteria();
pageable.setFilters(criteria.getFilters());
pageable.setSort(criteria.getSort());
pageable.setPage(0);
pageable.setSize(20);

// Execute
Page<Order> page = orderDAO.getQueryResultPage(pageable);
List<Order> list = orderDAO.getQueryResult(criteria);
```

### 3) Or accept JSON (example payload)

```json
{
  "filters": [
    {
      "type": "between",
      "field": "createdAt",
      "operator": "between",
      "start_date_time": "2025-08-01T00:00:00.000Z",
      "end_date_time":   "2025-08-31T23:59:59.999Z"
    },
    {
      "type": "contains",
      "field": "status",
      "operator": "in",
      "values": ["OPEN", "PENDING"]
    },
    {
      "type": "datetime",
      "field": "updatedAt",
      "operator": "after",
      "value": "2025-08-15T00:00:00.000Z"
    }
  ],
  "sort": {
    "ascending": ["createdAt"],
    "descending": ["totalAmount"]
  },
  "page": 0,
  "size": 20
}
```

Jackson will deserialize to `PageableCriteria` thanks to the `type` discriminator on each filter.

---

## How casting works (safety + correctness)

The access module’s handlers inspect the entity property type and cast input values accordingly:
- Numbers: `Integer`, `Long`, `Double`, `Float`, `BigDecimal`
- Booleans: `Boolean`
- Strings: `String`
- Temporal: `Instant` values converted to `Timestamp`, `LocalDateTime`, `java.util.Date`, or `java.sql.Date`
- Between/DateTime use `Expression.as(TargetType.class)` to feed correct types into `CriteriaBuilder` (`between`, `lessThan`, `greaterThan`, …).

If you pass an unsupported combination, you’ll get a clear `IllegalArgumentException`.

---

## Example: join a relation (simple equality in the join scope)

```java
var join = new JoinSpec();
join.setJoin("customer"); // root.join("customer")

var eqFilter = BasicFilter.builder()
    .field("lastName")
    .operator(BasicFilter.Operator.EQUAL)
    .value("Smith")
    .build();
join.setFilter(eqFilter);

var criteria = new Criteria();
criteria.setJoins(List.of(join));

List<Order> orders = orderDAO.getQueryResult(criteria);
```

> The default `AbstractDAO` applies a basic equality on the joined path using a `BasicFilter`. Extend `createJoinSpecification` if you need richer join logic.

---

## Testing

The project includes unit tests (JUnit 5 + Mockito) that validate handler behavior without a real database by mocking the JPA Criteria API.

Run:
```bash
mvn -q -DskipTests=false clean test
```

---

## Minimal app POM bits (Java 21)

```xml
<properties>
  <maven.compiler.release>21</maven.compiler.release>
  <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>

<dependencies>
  <!-- your JPA provider & Spring Data JPA -->
  <dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-jpa</artifactId>
    <version>3.3.3</version>
  </dependency>

  <!-- chadtalty modules -->
  <dependency>
    <groupId>com.chadtalty</groupId>
    <artifactId>commons-data-query</artifactId>
    <version>0.0.1</version>
  </dependency>
  <dependency>
    <groupId>com.chadtalty</groupId>
    <artifactId>commons-data-access</artifactId>
    <version>0.0.1</version>
  </dependency>
</dependencies>
```

---

## Notes & tips

- The JSON property keys use **snake_case**, but `field` values are **entity attribute paths** (usually camelCase).
- Add your own filter types by creating a new DTO + handler and registering it in `FilterHandlerFactory`.
- Keep page sizes sensible; `PageableCriteria` enforces `size` bounds in the DTO layer.

---

## License

