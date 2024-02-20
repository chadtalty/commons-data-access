package com.chadtalty.commons.data.access;

import java.util.List;

public interface EntityMapper<D, E> {

    D toDto(E entity);

    E toEntity(D dto);

    List<D> toDtos(List<E> entities);

    List<E> toEntities(List<D> dtos);
}
