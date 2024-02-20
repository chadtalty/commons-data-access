package com.chadtalty.commons.data.access;

import com.chadtalty.commons.data.query.Criteria;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DAO<T, R> {

    T findById(long id);

    List<T> findAll();

    Page<T> findAll(Pageable pageable);

    Long count();

    Page<T> getQueryResultPage(Criteria criteria);

    List<T> getQueryResult(Criteria criteria);

    boolean existsById(Long id);

    T save(T entity);

    R getRepository();
}
