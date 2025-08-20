package com.chadtalty.commons.data.access.filter.handler;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

import com.chadtalty.commons.data.access.testutil.MyEntity;
import com.chadtalty.commons.data.query.BasicFilter;
import com.chadtalty.commons.data.query.FilterType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"rawtypes","unchecked"})
class BasicFilterHandlerTest {

    FilterHandlerFactory<MyEntity> factory;
    BasicFilterHandler<MyEntity> handler;

    @BeforeEach
    void setUp() {
        factory = mock(FilterHandlerFactory.class);
        handler = new BasicFilterHandler<>(factory);
        handler.init();
        verify(factory).register(eq(FilterType.BASIC), eq(handler));
    }

    @Test
    void equal_integer_casts() {
        Root<MyEntity> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<MyEntity> cq = mock(CriteriaQuery.class);

        Path<Integer> path = mock(Path.class);
        when(root.get("age")).thenReturn((Path) path);              // Root#get returns Path<Object>
        when(path.getJavaType()).thenReturn((Class) Integer.class);  // avoid capture mismatch

        Predicate p = mock(Predicate.class);
        when(cb.equal(path, 30)).thenReturn(p);

        var filter = BasicFilter.builder().field("age")
                .operator(BasicFilter.Operator.EQUAL).value("30").build();

        Predicate result = handler.handle(filter).toPredicate(root, cq, cb);

        verify(cb).equal(path, 30);
        assertSame(p, result);
    }

    @Test
    void greaterThanOrEqual_uses_casted_equal_branch() {
        Root<MyEntity> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<MyEntity> cq = mock(CriteriaQuery.class);

        Path<Number> path = mock(Path.class);
        when(root.get("age")).thenReturn((Path) path);
        when(path.getJavaType()).thenReturn((Class) Integer.class);

        Predicate pGt = mock(Predicate.class), pEq = mock(Predicate.class), pOr = mock(Predicate.class);
        when(cb.gt(path, 10)).thenReturn(pGt);
        when(cb.equal(path, 10)).thenReturn(pEq);
        when(cb.or(pGt, pEq)).thenReturn(pOr);

        var filter = BasicFilter.builder().field("age")
                .operator(BasicFilter.Operator.GREATER_THAN_OR_EQUAL)
                .value("10").build();

        Predicate result = handler.handle(filter).toPredicate(root, cq, cb);

        verify(cb).gt(path, 10);
        verify(cb).equal(path, 10);
        verify(cb).or(pGt, pEq);
        assertSame(pOr, result);
    }
}
