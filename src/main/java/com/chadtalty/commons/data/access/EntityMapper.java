package com.chadtalty.commons.data.access;

import java.util.List;

/**
 * Simple mapper contract between entity and DTO types.
 *
 * @param <D> DTO type
 * @param <E> entity type
 */
public interface EntityMapper<D, E> {

    D toDto(E entity);

    E toEntity(D dto);

    List<D> toDtos(List<E> entities);

    List<E> toEntities(List<D> dtos);
}
