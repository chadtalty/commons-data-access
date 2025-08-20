package com.chadtalty.commons.data.access.filter.handler;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

import com.chadtalty.commons.data.access.testutil.MyEntity;
import com.chadtalty.commons.data.query.DateTimeFilter;
import com.chadtalty.commons.data.query.FilterType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.sql.Timestamp;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DateTimeFilterHandler}.
 *
 * Uses typed Path & Expression and targets the (Expression, Y) overload explicitly.
 */
@SuppressWarnings({"rawtypes","unchecked"})
class DateTimeFilterHandlerTest {

    FilterHandlerFactory<MyEntity> factory;
    DateTimeFilterHandler<MyEntity> handler;

    @BeforeEach
    void setUp() {
        factory = mock(FilterHandlerFactory.class);
        handler = new DateTimeFilterHandler<>(factory);
        handler.init();
        verify(factory).register(eq(FilterType.DATE_TIME), eq(handler));
    }

    @Test
    void before_uses_temporal_cast() {
        Root<MyEntity> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<MyEntity> cq = mock(CriteriaQuery.class);

        // Path createdAt is a Timestamp-mapped attribute
        Path<Timestamp> path = mock(Path.class);
        when(root.get("createdAt")).thenReturn((Path) path);                 // Root#get returns Path<Object>
        when(path.getJavaType()).thenReturn((Class) Timestamp.class);

        // production code: path.as(Timestamp.class)
        Expression<Timestamp> expr = mock(Expression.class);
        when(path.as(Timestamp.class)).thenReturn(expr);

        // Stub the (Expression, Y) overload explicitly with a generic witness
        Predicate p = mock(Predicate.class);
        when(cb.<Timestamp>lessThan(eq(expr), any(Timestamp.class))).thenReturn(p);

        var filter = DateTimeFilter.builder()
                .field("createdAt")
                .operator(DateTimeFilter.Operator.BEFORE)
                .value(Instant.parse("2025-08-20T00:00:00Z"))
                .build();

        Predicate result = handler.handle(filter).toPredicate(root, cq, cb);

        verify(path).as(Timestamp.class);
        // IMPORTANT: use any(Timestamp.class) to avoid overload ambiguity
        verify(cb).<Timestamp>lessThan(eq(expr), any(Timestamp.class));
        assertSame(p, result);
    }
}
