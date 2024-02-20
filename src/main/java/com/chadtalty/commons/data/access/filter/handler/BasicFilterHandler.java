package com.chadtalty.commons.data.access.filter.handler;

import com.chadtalty.commons.data.query.Filter;
import com.chadtalty.commons.data.query.filter.BasicFilter;
import com.chadtalty.commons.data.query.filter.FilterType;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BasicFilterHandler<T extends Object> implements FilterHandler<T> {

    private final FilterHandlerFactory<T> factory;

    @PostConstruct
    public void init() {
        this.factory.register(FilterType.BASIC, this);
    }

    @Override
    public Specification<T> handle(@Valid Filter f) {

        BasicFilter filter = (BasicFilter) f;

        switch (filter.getOperator()) {
            case EQUAL:
                return (root, query, cb) -> cb.equal(
                        root.get(filter.getField()),
                        castToRequiredType(root.get(filter.getField()).getJavaType(), filter.getValue()));

            case NOT_EQUAL:
                return (root, query, cb) -> cb.notEqual(root.get(filter.getField()), filter.getValue());

            case GREATER_THAN:
                return (root, query, cb) -> cb.gt(root.get(filter.getField()), (Number)
                        castToRequiredType(root.get(filter.getField()).getJavaType(), filter.getValue()));

            case LESS_THAN:
                return (root, query, cb) -> cb.lt(root.get(filter.getField()), (Number)
                        castToRequiredType(root.get(filter.getField()).getJavaType(), filter.getValue()));

            case GREATER_THAN_OR_EQUAL:
                return (root, query, cb) -> cb.or(
                        cb.gt(root.get(filter.getField()), (Number)
                                castToRequiredType(root.get(filter.getField()).getJavaType(), filter.getValue())),
                        cb.equal(root.get(filter.getField()), filter.getValue()));

            case LESS_THAN_OR_EQUAL:
                return (root, query, cb) -> cb.or(
                        cb.lt(root.get(filter.getField()), (Number)
                                castToRequiredType(root.get(filter.getField()).getJavaType(), filter.getValue())),
                        cb.equal(root.get(filter.getField()), filter.getValue()));

            default:
                throw new RuntimeException("Operation not supported.");
        }
    }

    private Object castToRequiredType(Class<?> fieldType, String value) {
        if (fieldType.isAssignableFrom(Double.class)) {
            return Double.valueOf(value);
        } else if (fieldType.isAssignableFrom(Integer.class)) {
            return Integer.valueOf(value);
        } else if (fieldType.isAssignableFrom(String.class)) {
            return value;
        }
        return null;
    }
}
