package com.chadtalty.commons.data.access.filter.handler;

import com.calflany.data.jpa.filter.DateTimeFilter;
import com.calflany.data.jpa.filter.Filter;
import com.calflany.data.jpa.filter.FilterType;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import java.sql.Timestamp;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DateTimeFilterHandler<T extends Object> implements FilterHandler<T> {

    private final FilterHandlerFactory<T> factory;

    @PostConstruct
    public void init() {
        this.factory.register(FilterType.DATE_TIME, this);
    }

    @Override
    public Specification<T> handle(@Valid Filter f) {

        DateTimeFilter filter = (DateTimeFilter) f;

        switch (filter.getOperator()) {
            case AFTER:
                return (root, query, cb) -> cb.greaterThan(root.get(filter.getField()), (Timestamp)
                        castToRequiredType(root.get(filter.getField()).getJavaType(), filter.getValue()));

            case AFTER_OR_EQUAL:
                return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(filter.getField()), (Timestamp)
                        castToRequiredType(root.get(filter.getField()).getJavaType(), filter.getValue()));

            case BEFORE:
                return (root, query, cb) -> cb.lessThan(root.get(filter.getField()), (Timestamp)
                        castToRequiredType(root.get(filter.getField()).getJavaType(), filter.getValue()));

            case BEFORE_OR_EQUAL:
                return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(filter.getField()), (Timestamp)
                        castToRequiredType(root.get(filter.getField()).getJavaType(), filter.getValue()));

            case EQUAL:
                return (root, query, cb) -> cb.equal(
                        root.get(filter.getField()),
                        castToRequiredType(root.get(filter.getField()).getJavaType(), filter.getValue()));

            case NOT_EQUAL:
                return (root, query, cb) -> cb.notEqual(root.get(filter.getField()), filter.getValue());

            default:
                throw new RuntimeException("Operation not supported.");
        }
    }

    private Object castToRequiredType(Class<?> fieldType, String value) {
        if (fieldType.isAssignableFrom(Timestamp.class)) {
            return Timestamp.from(Instant.parse(value));
        }
        return null;
    }
}
