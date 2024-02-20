package com.calflany.commons.data.access.filter.handler;

import com.calflany.data.jpa.filter.Filter;
import org.springframework.data.jpa.domain.Specification;

public interface FilterHandler<T extends Object> {

    void init();

    Specification<T> handle(Filter filter);
}
