package com.chadtalty.commons.data.access.filter.handler;

import com.chadtalty.commons.data.query.BasicFilter;
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
 * Translates {@link BasicFilter} into numeric/string comparison {@link Specification}s.
 *
 * <p>Supported operators: EQUAL, NOT_EQUAL, GREATER_THAN, LESS_THAN, GREATER_THAN_OR_EQUAL,
 * LESS_THAN_OR_EQUAL.
 */
@Component
@RequiredArgsConstructor
public class BasicFilterHandler<T> extends AbstractFilterHandler<T> {

    private final FilterHandlerFactory<T> factory;

    private final Map<BasicFilter.Operator, Function<BasicFilter, Specification<T>>> operations =
            new EnumMap<>(BasicFilter.Operator.class);

    @PostConstruct
    public void init() {
        factory.register(FilterType.BASIC, this);
        operations.put(BasicFilter.Operator.EQUAL, this::equal);
        operations.put(BasicFilter.Operator.NOT_EQUAL, this::notEqual);
        operations.put(BasicFilter.Operator.GREATER_THAN, this::greaterThan);
        operations.put(BasicFilter.Operator.LESS_THAN, this::lessThan);
        operations.put(BasicFilter.Operator.GREATER_THAN_OR_EQUAL, this::greaterThanOrEqual);
        operations.put(BasicFilter.Operator.LESS_THAN_OR_EQUAL, this::lessThanOrEqual);
    }

    @Override
    public Specification<T> handle(@Valid Filter f) {
        BasicFilter filter = (BasicFilter) f;
        return operations
                .getOrDefault(filter.getOperator(), this::unsupportedOperation)
                .apply(filter);
    }

    private Specification<T> equal(BasicFilter filter) {
        return (root, query, cb) -> cb.equal(
                root.get(filter.getField()),
                castToRequiredType(root.get(filter.getField()).getJavaType(), filter.getValue()));
    }

    private Specification<T> notEqual(BasicFilter filter) {
        return (root, query, cb) -> cb.notEqual(
                root.get(filter.getField()),
                castToRequiredType(root.get(filter.getField()).getJavaType(), filter.getValue()));
    }

    private Specification<T> greaterThan(BasicFilter filter) {
        return (root, query, cb) -> cb.gt(root.get(filter.getField()), (Number)
                castToRequiredType(root.get(filter.getField()).getJavaType(), filter.getValue()));
    }

    private Specification<T> lessThan(BasicFilter filter) {
        return (root, query, cb) -> cb.lt(root.get(filter.getField()), (Number)
                castToRequiredType(root.get(filter.getField()).getJavaType(), filter.getValue()));
    }

    /**
     * BUGFIX: equality branch now casts the value (was using raw string).
     */
    private Specification<T> greaterThanOrEqual(BasicFilter filter) {
        return (root, query, cb) -> cb.or(
                cb.gt(root.get(filter.getField()), (Number)
                        castToRequiredType(root.get(filter.getField()).getJavaType(), filter.getValue())),
                cb.equal(
                        root.get(filter.getField()),
                        castToRequiredType(root.get(filter.getField()).getJavaType(), filter.getValue())));
    }

    /**
     * BUGFIX: equality branch now casts the value (was using raw string).
     */
    private Specification<T> lessThanOrEqual(BasicFilter filter) {
        return (root, query, cb) -> cb.or(
                cb.lt(root.get(filter.getField()), (Number)
                        castToRequiredType(root.get(filter.getField()).getJavaType(), filter.getValue())),
                cb.equal(
                        root.get(filter.getField()),
                        castToRequiredType(root.get(filter.getField()).getJavaType(), filter.getValue())));
    }

    private Specification<T> unsupportedOperation(BasicFilter filter) {
        throw new UnsupportedOperationException("Operation not supported: " + filter.getOperator());
    }
}
