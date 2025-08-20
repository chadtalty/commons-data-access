package com.chadtalty.commons.data.access.filter.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import org.junit.jupiter.api.Test;

/** Unit tests for common casting logic. */
class AbstractFilterHandlerTest {

    private final AbstractFilterHandler<Object> handler = new AbstractFilterHandler<>() {
        @Override public void init() {}
        @Override public org.springframework.data.jpa.domain.Specification<Object> handle(
                com.chadtalty.commons.data.query.Filter filter) { return null; }
    };

    @Test void cast_fromString_primitivesAndCommon() {
        assertEquals(42, handler.castToRequiredType(Integer.class, "42"));
        assertEquals(42L, handler.castToRequiredType(Long.class, "42"));
        assertEquals(3.14D, handler.castToRequiredType(Double.class, "3.14"));
        assertEquals(3.14F, handler.castToRequiredType(Float.class, "3.14"));
        assertEquals(Boolean.TRUE, handler.castToRequiredType(Boolean.class, "true"));
        assertEquals(new BigDecimal("123.45"), handler.castToRequiredType(BigDecimal.class, "123.45"));
        assertEquals("abc", handler.castToRequiredType(String.class, "abc"));
    }

    @Test void cast_fromString_temporal() {
        String iso = "2025-08-20T12:34:56.000Z";
        assertInstanceOf(Date.class, handler.castToRequiredType(Date.class, iso));
        assertInstanceOf(LocalDateTime.class, handler.castToRequiredType(LocalDateTime.class, "2025-08-20T12:34:56"));
    }

    @Test void cast_fromInstant_temporal() {
        Instant now = Instant.parse("2025-08-20T12:00:00Z");
        assertInstanceOf(Timestamp.class, handler.castToRequiredType(Timestamp.class, now));
        assertInstanceOf(LocalDateTime.class, handler.castToRequiredType(LocalDateTime.class, now));
        assertInstanceOf(Date.class, handler.castToRequiredType(Date.class, now));
        assertInstanceOf(java.sql.Date.class, handler.castToRequiredType(java.sql.Date.class, now));
    }

    @Test void cast_unsupported() {
        assertThrows(IllegalArgumentException.class,
                () -> handler.castToRequiredType(Object.class, "x"));
    }

    public Object castToRequiredType(Class<?> fieldType, Instant value) {
    if (fieldType.isAssignableFrom(Timestamp.class)) {
        return Timestamp.from(value);
    } else if (fieldType.isAssignableFrom(LocalDateTime.class)) {
        return LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    } else if (fieldType.isAssignableFrom(java.sql.Date.class)) {
        return new java.sql.Date(value.toEpochMilli());     // <-- add this
    } else if (fieldType.isAssignableFrom(java.util.Date.class)) {
        return java.util.Date.from(value);
    }
    throw new IllegalArgumentException("Unsupported field type: " + fieldType.getName());
}
}
