package com.chadtalty.commons.data.access.testutil;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/** Test-only entity skeleton (no JPA annotations needed for handler unit tests). */
public class MyEntity {
    public Integer age;
    public String status;
    public Double price;
    public BigDecimal amount;
    public Boolean active;
    public Timestamp createdAt;
    public LocalDateTime updatedAt;
}
