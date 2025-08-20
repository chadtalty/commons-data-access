package com.chadtalty.commons.data.access.filter.handler;

import com.chadtalty.commons.data.query.Filter;
import org.springframework.data.jpa.domain.Specification;

/**
 * Strategy for translating a {@link Filter} instance into a JPA {@link Specification}.
 *
 * <p>Concrete handlers implement operator-specific translations (e.g., EQUAL, IN, BETWEEN) and are
 * typically registered with the {@link FilterHandlerFactory} during {@code @PostConstruct}.
 *
 * @param <T> entity type managed by the resulting {@link Specification}.
 */
public interface FilterHandler<T> {

    /** Registration hook (usually called by Spring via {@code @PostConstruct}). */
    void init();

    /**
     * Translates the given {@link Filter} into a {@link Specification}.
     *
     * @param filter a validated filter DTO.
     * @return a non-null specification that can be combined by the caller.
     */
    Specification<T> handle(Filter filter);
}
