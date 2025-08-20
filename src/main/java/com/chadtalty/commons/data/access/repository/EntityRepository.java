package com.chadtalty.commons.data.access.repository;

import java.io.Serializable;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Base repository contract for domain entities.
 *
 * @param <E> entity type
 * @param <I> identifier type
 */
@NoRepositoryBean
public interface EntityRepository<E, I extends Serializable> extends JpaRepositoryImplementation<E, I> {}
