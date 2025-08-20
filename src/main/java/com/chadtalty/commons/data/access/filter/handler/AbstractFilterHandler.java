package com.chadtalty.commons.data.access.filter.handler;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Base class for filter handlers providing common type-conversion utilities.
 *
 * <p>These helpers convert incoming JSON string values (or {@link Instant}s) into the Java
 * property type that lives on the JPA entity, so the generated {@code CriteriaBuilder}
 * predicates receive properly-typed arguments (e.g., converting "123" to {@code Integer}).
 *
 * <p>Implementations should always call these helpers before passing values into
 * {@code CriteriaBuilder} methods like {@code equal}, {@code gt}, etc.
 *
 * @param <T> entity type (for {@link org.springframework.data.jpa.domain.Specification}).
 */
public abstract class AbstractFilterHandler<T> implements FilterHandler<T> {

    /**
     * Casts a text value (from JSON) to the given JPA property type.
     *
     * @param fieldType the target Java type on the entity property.
     * @param value     the raw JSON string value.
     * @return converted instance compatible with JPA property type.
     * @throws IllegalArgumentException if the type is unsupported or the value cannot be parsed.
     */
    public Object castToRequiredType(Class<?> fieldType, String value) {
        if (fieldType.isAssignableFrom(Double.class)) {
            return Double.valueOf(value);
        } else if (fieldType.isAssignableFrom(Integer.class)) {
            return Integer.valueOf(value);
        } else if (fieldType.isAssignableFrom(Long.class)) {
            return Long.valueOf(value);
        } else if (fieldType.isAssignableFrom(Float.class)) {
            return Float.valueOf(value);
        } else if (fieldType.isAssignableFrom(Boolean.class)) {
            return Boolean.valueOf(value);
        } else if (fieldType.isAssignableFrom(BigDecimal.class)) {
            return new BigDecimal(value);
        } else if (fieldType.isAssignableFrom(Date.class)) {
            // Expect ISO-8601 instant (e.g., 2025-08-20T12:34:56.000Z)
            return Date.from(Instant.parse(value));
        } else if (fieldType.isAssignableFrom(LocalDateTime.class)) {
            // Accept ISO local date-time; fallback to Instant -> UTC if offset present
            try {
                return LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception ignored) {
                return LocalDateTime.ofInstant(Instant.parse(value), ZoneOffset.UTC);
            }
        } else if (fieldType.isAssignableFrom(String.class)) {
            return value;
        }
        throw new IllegalArgumentException("Unsupported field type: " + fieldType.getName());
    }

    /**
     * Casts an {@link Instant} (already parsed at the DTO layer) to the target Java temporal type.
     *
     * @param fieldType the target Java type on the entity property.
     * @param value     the instant value.
     * @return converted instance (e.g., {@link Timestamp}, {@link LocalDateTime}, or {@link Date}).
     * @throws IllegalArgumentException if the type is unsupported.
     */
    public Object castToRequiredType(Class<?> fieldType, Instant value) {
        if (fieldType.isAssignableFrom(Timestamp.class)) {
            return Timestamp.from(value);
        } else if (fieldType.isAssignableFrom(LocalDateTime.class)) {
            return LocalDateTime.ofInstant(value, ZoneOffset.UTC);
        } else if (fieldType.isAssignableFrom(java.sql.Date.class)) {
            // java.sql.Date is date-only (time truncated by JDBC), but Instant epoch ms is fine
            return new java.sql.Date(value.toEpochMilli());
        } else if (fieldType.isAssignableFrom(Date.class)) {
            return Date.from(value);
        }
        throw new IllegalArgumentException("Unsupported field type: " + fieldType.getName());
    }
}
