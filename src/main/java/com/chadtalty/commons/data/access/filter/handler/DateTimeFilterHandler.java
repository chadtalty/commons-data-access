package com.chadtalty.commons.data.access.filter.handler;

import com.chadtalty.commons.data.query.DateTimeFilter;
import com.chadtalty.commons.data.query.Filter;
import com.chadtalty.commons.data.query.FilterType;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * Translates {@link DateTimeFilter} into temporal comparison {@link Specification}s.
 *
 * <p>Supports entity properties mapped as {@link Timestamp}, {@link LocalDateTime},
 * {@link java.sql.Date}, or {@link java.util.Date}. The handler re-types the path using
 * {@code Path.as(TargetType.class)} and feeds correctly-typed values to {@link jakarta.persistence.criteria.CriteriaBuilder}.
 */
@Component
@RequiredArgsConstructor
public class DateTimeFilterHandler<T> extends AbstractFilterHandler<T> {

    private final FilterHandlerFactory<T> factory;

    private final Map<DateTimeFilter.Operator, Function<DateTimeFilter, Specification<T>>> operations =
            new EnumMap<>(DateTimeFilter.Operator.class);

    @PostConstruct
    public void init() {
        factory.register(FilterType.DATE_TIME, this);
        operations.put(DateTimeFilter.Operator.AFTER, this::after);
        operations.put(DateTimeFilter.Operator.AFTER_OR_EQUAL, this::afterOrEqual);
        operations.put(DateTimeFilter.Operator.BEFORE, this::before);
        operations.put(DateTimeFilter.Operator.BEFORE_OR_EQUAL, this::beforeOrEqual);
        operations.put(DateTimeFilter.Operator.EQUAL, this::equal);
        operations.put(DateTimeFilter.Operator.NOT_EQUAL, this::notEqual);
    }

    @Override
    public Specification<T> handle(@Valid Filter f) {
        DateTimeFilter filter = (DateTimeFilter) f;
        return operations
                .getOrDefault(filter.getOperator(), this::unsupportedOperation)
                .apply(filter);
    }

    private Specification<T> after(DateTimeFilter filter) {
        return (root, query, cb) -> {
            Path<?> path = root.get(filter.getField());
            Class<?> type = path.getJavaType();

            if (Timestamp.class.isAssignableFrom(type)) {
                Expression<Timestamp> expr = path.as(Timestamp.class);
                Timestamp v = (Timestamp) castToRequiredType(Timestamp.class, filter.getValue());
                return cb.greaterThan(expr, v); // (Expression, Y)
            } else if (LocalDateTime.class.isAssignableFrom(type)) {
                Expression<LocalDateTime> expr = path.as(LocalDateTime.class);
                LocalDateTime v = (LocalDateTime) castToRequiredType(LocalDateTime.class, filter.getValue());
                return cb.greaterThan(expr, v);
            } else if (java.sql.Date.class.isAssignableFrom(type)) {
                Expression<java.sql.Date> expr = path.as(java.sql.Date.class);
                java.sql.Date v = (java.sql.Date) castToRequiredType(java.sql.Date.class, filter.getValue());
                return cb.greaterThan(expr, v);
            } else if (Date.class.isAssignableFrom(type)) {
                Expression<Date> expr = path.as(Date.class);
                Date v = (Date) castToRequiredType(Date.class, filter.getValue());
                return cb.greaterThan(expr, v);
            }
            throw unsupportedTemporal(filter.getField(), type);
        };
    }

    private Specification<T> afterOrEqual(DateTimeFilter filter) {
        return (root, query, cb) -> {
            Path<?> path = root.get(filter.getField());
            Class<?> type = path.getJavaType();

            if (Timestamp.class.isAssignableFrom(type)) {
                Expression<Timestamp> expr = path.as(Timestamp.class);
                Timestamp v = (Timestamp) castToRequiredType(Timestamp.class, filter.getValue());
                return cb.greaterThanOrEqualTo(expr, v);
            } else if (LocalDateTime.class.isAssignableFrom(type)) {
                Expression<LocalDateTime> expr = path.as(LocalDateTime.class);
                LocalDateTime v = (LocalDateTime) castToRequiredType(LocalDateTime.class, filter.getValue());
                return cb.greaterThanOrEqualTo(expr, v);
            } else if (java.sql.Date.class.isAssignableFrom(type)) {
                Expression<java.sql.Date> expr = path.as(java.sql.Date.class);
                java.sql.Date v = (java.sql.Date) castToRequiredType(java.sql.Date.class, filter.getValue());
                return cb.greaterThanOrEqualTo(expr, v);
            } else if (Date.class.isAssignableFrom(type)) {
                Expression<Date> expr = path.as(Date.class);
                Date v = (Date) castToRequiredType(Date.class, filter.getValue());
                return cb.greaterThanOrEqualTo(expr, v);
            }
            throw unsupportedTemporal(filter.getField(), type);
        };
    }

    private Specification<T> before(DateTimeFilter filter) {
        return (root, query, cb) -> {
            Path<?> path = root.get(filter.getField());
            Class<?> type = path.getJavaType();

            if (Timestamp.class.isAssignableFrom(type)) {
                Expression<Timestamp> expr = path.as(Timestamp.class);
                Timestamp v = (Timestamp) castToRequiredType(Timestamp.class, filter.getValue());
                return cb.lessThan(expr, v);
            } else if (LocalDateTime.class.isAssignableFrom(type)) {
                Expression<LocalDateTime> expr = path.as(LocalDateTime.class);
                LocalDateTime v = (LocalDateTime) castToRequiredType(LocalDateTime.class, filter.getValue());
                return cb.lessThan(expr, v);
            } else if (java.sql.Date.class.isAssignableFrom(type)) {
                Expression<java.sql.Date> expr = path.as(java.sql.Date.class);
                java.sql.Date v = (java.sql.Date) castToRequiredType(java.sql.Date.class, filter.getValue());
                return cb.lessThan(expr, v);
            } else if (Date.class.isAssignableFrom(type)) {
                Expression<Date> expr = path.as(Date.class);
                Date v = (Date) castToRequiredType(Date.class, filter.getValue());
                return cb.lessThan(expr, v);
            }
            throw unsupportedTemporal(filter.getField(), type);
        };
    }

    private Specification<T> beforeOrEqual(DateTimeFilter filter) {
        return (root, query, cb) -> {
            Path<?> path = root.get(filter.getField());
            Class<?> type = path.getJavaType();

            if (Timestamp.class.isAssignableFrom(type)) {
                Expression<Timestamp> expr = path.as(Timestamp.class);
                Timestamp v = (Timestamp) castToRequiredType(Timestamp.class, filter.getValue());
                return cb.lessThanOrEqualTo(expr, v);
            } else if (LocalDateTime.class.isAssignableFrom(type)) {
                Expression<LocalDateTime> expr = path.as(LocalDateTime.class);
                LocalDateTime v = (LocalDateTime) castToRequiredType(LocalDateTime.class, filter.getValue());
                return cb.lessThanOrEqualTo(expr, v);
            } else if (java.sql.Date.class.isAssignableFrom(type)) {
                Expression<java.sql.Date> expr = path.as(java.sql.Date.class);
                java.sql.Date v = (java.sql.Date) castToRequiredType(java.sql.Date.class, filter.getValue());
                return cb.lessThanOrEqualTo(expr, v);
            } else if (Date.class.isAssignableFrom(type)) {
                Expression<Date> expr = path.as(Date.class);
                Date v = (Date) castToRequiredType(Date.class, filter.getValue());
                return cb.lessThanOrEqualTo(expr, v);
            }
            throw unsupportedTemporal(filter.getField(), type);
        };
    }

    private Specification<T> equal(DateTimeFilter filter) {
        return (root, query, cb) -> {
            Path<?> path = root.get(filter.getField());
            Class<?> type = path.getJavaType();

            if (Timestamp.class.isAssignableFrom(type)) {
                Expression<Timestamp> expr = path.as(Timestamp.class);
                Timestamp v = (Timestamp) castToRequiredType(Timestamp.class, filter.getValue());
                return cb.equal(expr, v);
            } else if (LocalDateTime.class.isAssignableFrom(type)) {
                Expression<LocalDateTime> expr = path.as(LocalDateTime.class);
                LocalDateTime v = (LocalDateTime) castToRequiredType(LocalDateTime.class, filter.getValue());
                return cb.equal(expr, v);
            } else if (java.sql.Date.class.isAssignableFrom(type)) {
                Expression<java.sql.Date> expr = path.as(java.sql.Date.class);
                java.sql.Date v = (java.sql.Date) castToRequiredType(java.sql.Date.class, filter.getValue());
                return cb.equal(expr, v);
            } else if (Date.class.isAssignableFrom(type)) {
                Expression<Date> expr = path.as(Date.class);
                Date v = (Date) castToRequiredType(Date.class, filter.getValue());
                return cb.equal(expr, v);
            }
            throw unsupportedTemporal(filter.getField(), type);
        };
    }

    private Specification<T> notEqual(DateTimeFilter filter) {
        return (root, query, cb) -> {
            Path<?> path = root.get(filter.getField());
            Class<?> type = path.getJavaType();

            if (Timestamp.class.isAssignableFrom(type)) {
                Expression<Timestamp> expr = path.as(Timestamp.class);
                Timestamp v = (Timestamp) castToRequiredType(Timestamp.class, filter.getValue());
                return cb.notEqual(expr, v);
            } else if (LocalDateTime.class.isAssignableFrom(type)) {
                Expression<LocalDateTime> expr = path.as(LocalDateTime.class);
                LocalDateTime v = (LocalDateTime) castToRequiredType(LocalDateTime.class, filter.getValue());
                return cb.notEqual(expr, v);
            } else if (java.sql.Date.class.isAssignableFrom(type)) {
                Expression<java.sql.Date> expr = path.as(java.sql.Date.class);
                java.sql.Date v = (java.sql.Date) castToRequiredType(java.sql.Date.class, filter.getValue());
                return cb.notEqual(expr, v);
            } else if (Date.class.isAssignableFrom(type)) {
                Expression<Date> expr = path.as(Date.class);
                Date v = (Date) castToRequiredType(Date.class, filter.getValue());
                return cb.notEqual(expr, v);
            }
            throw unsupportedTemporal(filter.getField(), type);
        };
    }

    private Specification<T> unsupportedOperation(DateTimeFilter filter) {
        throw new UnsupportedOperationException("Operation not supported: " + filter.getOperator());
    }

    private IllegalArgumentException unsupportedTemporal(String field, Class<?> type) {
        return new IllegalArgumentException(
                "Temporal comparison only supported on Timestamp/LocalDateTime/Date. Field '"
                        + field + "' has type " + type.getName());
    }
}
