package com.chadtalty.commons.data.access.filter.handler;

import com.calflany.data.jpa.filter.FilterType;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class FilterHandlerFactory<T> {

    private final Map<FilterType, FilterHandler<T>> filterHandlerMap = new HashMap<>();

    public void register(FilterType type, FilterHandler<T> handler) {
        this.filterHandlerMap.put(type, handler);
    }

    public FilterHandler<T> getFilterHandler(FilterType type) {
        return filterHandlerMap.get(type);
    }
}
