package com.chadtalty.commons.data.access;

import com.chadtalty.commons.data.query.Criteria;
import com.chadtalty.commons.data.query.PageableCriteria;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Generic DAO contract exposing common CRUD and query operations.
 *
 * @param <T> entity type
 * @param <R> repository type
 */
public interface DAO<T, R> {

    T findById(long id);

    List<T> findAll();

    List<T> getQueryResult(Criteria criteria);

    Page<T> findAll(Pageable pageable);

    Page<T> getQueryResultPage(PageableCriteria criteria);

    Long count();

    boolean existsById(Long id);

    T save(T entity);

    R getRepository();
}
