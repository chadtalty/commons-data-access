package com.chadtalty.commons.data.access.filter.handler;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.chadtalty.commons.data.query.BetweenFilter;
import com.chadtalty.commons.data.query.Filter;
import com.chadtalty.commons.data.query.FilterType;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.Path;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Translates {@link BetweenFilter} into a temporal range {@link Specification}.
 *
 * <p>
 * Assumes the mapped entity property is temporal (e.g., {@link Timestamp},
 * {@code LocalDateTime}, {@code Date}).
 */
@Component
@RequiredArgsConstructor
public class BetweenFilterHandler<T> extends AbstractFilterHandler<T> {

    private final FilterHandlerFactory<T> factory;

    private final Map<BetweenFilter.Operator, Function<BetweenFilter, Specification<T>>> operations = new EnumMap<>(
            BetweenFilter.Operator.class);

    @PostConstruct
    public void init() {
        factory.register(FilterType.BETWEEN, this);
        operations.put(BetweenFilter.Operator.BETWEEN, this::between);
    }

    @Override
    public Specification<T> handle(@Valid Filter f) {
        BetweenFilter filter = (BetweenFilter) f;
        return operations
                .getOrDefault(filter.getOperator(), this::unsupportedOperation)
                .apply(filter);
    }

    private Specification<T> between(BetweenFilter filter) {
        return (root, query, cb) -> {
            Path<?> path = root.get(filter.getField());
            Class<?> javaType = path.getJavaType();

            if (Timestamp.class.isAssignableFrom(javaType)) {
                var expr = root.get(filter.getField()).as(Timestamp.class);
                Timestamp start = (Timestamp) castToRequiredType(Timestamp.class, filter.getStartDateTime());
                Timestamp end = (Timestamp) castToRequiredType(Timestamp.class, filter.getEndDateTime());
                return cb.between(expr, start, end);

            } else if (LocalDateTime.class.isAssignableFrom(javaType)) {
                var expr = root.get(filter.getField()).as(LocalDateTime.class);
                LocalDateTime start = (LocalDateTime) castToRequiredType(LocalDateTime.class,
                        filter.getStartDateTime());
                LocalDateTime end = (LocalDateTime) castToRequiredType(LocalDateTime.class, filter.getEndDateTime());
                return cb.between(expr, start, end);

            } else if (java.sql.Date.class.isAssignableFrom(javaType)) {
                var expr = root.get(filter.getField()).as(java.sql.Date.class);
                java.sql.Date start = (java.sql.Date) castToRequiredType(java.sql.Date.class,
                        filter.getStartDateTime());
                java.sql.Date end = (java.sql.Date) castToRequiredType(java.sql.Date.class, filter.getEndDateTime());
                return cb.between(expr, start, end);

            } else if (java.util.Date.class.isAssignableFrom(javaType)) {
                var expr = root.get(filter.getField()).as(java.util.Date.class);
                java.util.Date start = (java.util.Date) castToRequiredType(java.util.Date.class,
                        filter.getStartDateTime());
                java.util.Date end = (java.util.Date) castToRequiredType(java.util.Date.class, filter.getEndDateTime());
                return cb.between(expr, start, end);
            }

            throw new IllegalArgumentException(
                    "Between is only supported on temporal types (Timestamp/LocalDateTime/Date). Field '" +
                            filter.getField() + "' has type " + javaType.getName());
        };
    }

    private Specification<T> unsupportedOperation(BetweenFilter filter) {
        throw new UnsupportedOperationException("Operation not supported: " + filter.getOperator());
    }
}
