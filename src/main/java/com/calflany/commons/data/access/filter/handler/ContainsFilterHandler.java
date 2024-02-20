package com.calflany.commons.data.access.filter.handler;

import com.calflany.data.jpa.filter.ContainsFilter;
import com.calflany.data.jpa.filter.Filter;
import com.calflany.data.jpa.filter.FilterType;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContainsFilterHandler<T extends Object> implements FilterHandler<T> {

    private final FilterHandlerFactory<T> factory;

    @PostConstruct
    public void init() {
        this.factory.register(FilterType.CONTAINS, this);
    }

    @Override
    public Specification<T> handle(@Valid Filter f) {

        ContainsFilter filter = (ContainsFilter) f;
        switch (filter.getOperator()) {
            case IN:
                return (root, query, criteriaBuilder) -> criteriaBuilder
                        .in(root.get(filter.getField()))
                        .value(castToRequiredType(root.get(filter.getField()).getJavaType(), filter.getValues()));
            default:
                throw new RuntimeException("Operation not supported.");
        }
    }

    private Object castToRequiredType(Class<?> fieldType, List<String> value) {
        List<Object> lists = new ArrayList<>();
        for (String s : value) {
            lists.add(castToRequiredType(fieldType, s));
        }
        return lists;
    }

    private Object castToRequiredType(Class<?> fieldType, String value) {
        if (fieldType.isAssignableFrom(Double.class)) {
            return Double.valueOf(value);
        } else if (fieldType.isAssignableFrom(Integer.class)) {
            return Integer.valueOf(value);
        }
        return null;
    }
}
