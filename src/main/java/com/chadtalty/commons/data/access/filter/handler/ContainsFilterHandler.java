package com.chadtalty.commons.data.access.filter.handler;

import com.chadtalty.commons.data.query.ContainsFilter;
import com.chadtalty.commons.data.query.Filter;
import com.chadtalty.commons.data.query.FilterType;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * Translates {@link ContainsFilter} into an {@code IN (...)} {@link Specification}.
 */
@Component
@RequiredArgsConstructor
public class ContainsFilterHandler<T> extends AbstractFilterHandler<T> {

    private final FilterHandlerFactory<T> factory;

    private final Map<ContainsFilter.Operator, Function<ContainsFilter, Specification<T>>> operations =
            new EnumMap<>(ContainsFilter.Operator.class);

    @PostConstruct
    public void init() {
        factory.register(FilterType.CONTAINS, this);
        operations.put(ContainsFilter.Operator.IN, this::in);
    }

    @Override
    public Specification<T> handle(@Valid Filter f) {
        ContainsFilter filter = (ContainsFilter) f;
        return operations
                .getOrDefault(filter.getOperator(), this::unsupportedOperation)
                .apply(filter);
    }

    /**
     * BUGFIX: build {@code in} predicate by adding each value, not by passing a List as a single value.
     */
    private Specification<T> in(ContainsFilter filter) {
        return (root, query, cb) -> {
            var path = root.get(filter.getField());
            var in = cb.in(path);
            Class<?> javaType = path.getJavaType();
            for (String raw : filter.getValues()) {
                in = in.value(castToRequiredType(javaType, raw));
            }
            return in;
        };
    }

    private Specification<T> unsupportedOperation(ContainsFilter filter) {
        throw new UnsupportedOperationException("Operation not supported: " + filter.getOperator());
    }
}
