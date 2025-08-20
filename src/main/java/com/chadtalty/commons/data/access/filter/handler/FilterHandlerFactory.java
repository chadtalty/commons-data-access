package com.chadtalty.commons.data.access.filter.handler;

import com.chadtalty.commons.data.query.FilterType;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Registry of {@link FilterHandler} implementations keyed by {@link FilterType}.
 *
 * <p>Filters are polymorphic; this factory returns the correct handler for a given filter type.
 */
@Component
public class FilterHandlerFactory<T> {

    private final Map<FilterType, FilterHandler<T>> filterHandlerMap = new HashMap<>();

    /** Registers a handler for a filter type (typically during {@code @PostConstruct}). */
    public void register(FilterType type, FilterHandler<T> handler) {
        this.filterHandlerMap.put(type, handler);
    }

    /** Looks up a previously-registered handler or returns {@code null} if none is registered. */
    public FilterHandler<T> getFilterHandler(FilterType type) {
        return filterHandlerMap.get(type);
    }
}
