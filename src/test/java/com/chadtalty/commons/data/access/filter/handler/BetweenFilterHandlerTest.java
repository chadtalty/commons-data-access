package com.chadtalty.commons.data.access.filter.handler;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

import com.chadtalty.commons.data.access.testutil.MyEntity;
import com.chadtalty.commons.data.query.BetweenFilter;
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

@SuppressWarnings({"rawtypes","unchecked"})
class BetweenFilterHandlerTest {

    FilterHandlerFactory<MyEntity> factory;
    BetweenFilterHandler<MyEntity> handler;

    @BeforeEach
    void setUp() {
        factory = mock(FilterHandlerFactory.class);
        handler = new BetweenFilterHandler<>(factory);
        handler.init();
        verify(factory).register(eq(FilterType.BETWEEN), eq(handler));
    }

    @Test
    void between_builds_predicate() {
        Root<MyEntity> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<MyEntity> cq = mock(CriteriaQuery.class);

        Path<Timestamp> path = mock(Path.class);
        when(root.get("createdAt")).thenReturn((Path) path);
        when(path.getJavaType()).thenReturn((Class) Timestamp.class);

        // production uses path.as(Timestamp.class)
        Expression<Timestamp> expr = mock(Expression.class);
        when(path.as(Timestamp.class)).thenReturn(expr);

        Predicate p = mock(Predicate.class);
        when(cb.between(eq(expr), any(Timestamp.class), any(Timestamp.class))).thenReturn(p);

        var filter = BetweenFilter.builder()
                .field("createdAt")
                .startDateTime(Instant.parse("2025-08-01T00:00:00Z"))
                .endDateTime(Instant.parse("2025-08-31T23:59:59Z"))
                .build();

        Predicate result = handler.handle(filter).toPredicate(root, cq, cb);

        verify(cb).between(eq(expr), any(Timestamp.class), any(Timestamp.class));
        assertSame(p, result);
    }
}
