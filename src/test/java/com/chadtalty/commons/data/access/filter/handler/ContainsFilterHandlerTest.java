package com.chadtalty.commons.data.access.filter.handler;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

import com.chadtalty.commons.data.access.testutil.MyEntity;
import com.chadtalty.commons.data.query.ContainsFilter;
import com.chadtalty.commons.data.query.FilterType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"rawtypes","unchecked"})
class ContainsFilterHandlerTest {

    FilterHandlerFactory<MyEntity> factory;
    ContainsFilterHandler<MyEntity> handler;

    @BeforeEach
    void setUp() {
        factory = mock(FilterHandlerFactory.class);
        handler = new ContainsFilterHandler<>(factory);
        handler.init();
        verify(factory).register(eq(FilterType.CONTAINS), eq(handler));
    }

    @Test
    void in_builds_values_individually() {
        Root<MyEntity> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<MyEntity> cq = mock(CriteriaQuery.class);

        Path<Integer> path = mock(Path.class);
        when(root.get("age")).thenReturn((Path) path);
        when(path.getJavaType()).thenReturn((Class) Integer.class);

        In<Integer> in = mock(In.class);
        when(cb.in(path)).thenReturn(in);
        when(in.value(1)).thenReturn(in);
        when(in.value(2)).thenReturn(in);

        var filter = ContainsFilter.builder()
                .field("age")
                .values(List.of("1", "2"))
                .build();

        Predicate result = handler.handle(filter).toPredicate(root, cq, cb);

        verify(cb).in(path);
        verify(in).value(1);
        verify(in).value(2);
        assertSame(in, result);
    }
}
