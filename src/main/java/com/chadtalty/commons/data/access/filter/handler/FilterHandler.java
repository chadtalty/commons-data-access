package com.chadtalty.commons.data.access.filter.handler;

import com.chadtalty.commons.data.query.Filter;
import org.springframework.data.jpa.domain.Specification;

public interface FilterHandler<T extends Object> {

    void init();

    Specification<T> handle(Filter filter);
}
