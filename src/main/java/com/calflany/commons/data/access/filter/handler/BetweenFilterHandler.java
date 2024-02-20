package com.calflany.commons.data.access.filter.handler;

import com.calflany.data.jpa.filter.BetweenFilter;
import com.calflany.data.jpa.filter.Filter;
import com.calflany.data.jpa.filter.FilterType;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BetweenFilterHandler<T extends Object> implements FilterHandler<T> {

    private final FilterHandlerFactory<T> factory;

    @PostConstruct
    public void init() {
        this.factory.register(FilterType.BETWEEN, this);
    }

    @Override
    public Specification<T> handle(@Valid Filter f) {

        BetweenFilter filter = (BetweenFilter) f;

        switch (filter.getOperator()) {
            case BETWEEN:
                return (root, query, cb) ->
                        cb.between(root.get(filter.getField()), filter.getStartDateTime(), filter.getEndDateTime());

            default:
                throw new RuntimeException("Operation not supported.");
        }
    }
}
