package com.chadtalty.commons.data.access;

import static org.springframework.data.jpa.domain.Specification.where;

import com.calflany.data.jpa.Criteria;
import com.calflany.data.jpa.JoinColumn;
import com.calflany.data.jpa.filter.BasicFilter;
import com.calflany.data.jpa.filter.Filter;
import com.chadtalty.commons.data.access.filter.handler.FilterHandlerFactory;
import com.chadtalty.commons.data.access.repository.EntityRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

/**
 * AbstractDAO provides abstract JpaRepository functionality.
 *
 * @param <E> Entity type
 * @param <R> EntityRepository type
 */
public abstract class AbstractDAO<E, R extends EntityRepository<E, Long>> implements DAO<E, R> {

    /**
     * EntityRepository.
     */
    @Autowired
    protected R repository;

    private FilterHandlerFactory<E> filterHandlerFactory;

    @Autowired
    public final void setFilterHandlerFactory(FilterHandlerFactory<E> filterHandlerFactory) {
        this.filterHandlerFactory = filterHandlerFactory;
    }

    /**
     * Find entity of type E by ID. Throws EntityNotFoundException if entity not
     * found.
     *
     * @param id Identifier
     * @return Entity of type E
     */
    @Override
    public E findById(long id) {
        return this.repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Entity not found: " + id));
    }

    /**
     * Returns a list all entities of type E/
     *
     * @return List of entities of type E
     */
    @Override
    public List<E> findAll() {
        return this.repository.findAll();
    }

    /**
     * Returns a Page of all entities of type E.
     * Utilizes Java Pagination which is a concept used to access the content by
     * using First Page, Second Page, etc.
     *
     * @param pageable
     * @return page of entities of type E
     */
    @Override
    public Page<E> findAll(Pageable pageable) {
        return this.repository.findAll(pageable);
    }

    /**
     * Returns the number of entities available.
     *
     * @return the number of entities.
     */
    @Override
    public Long count() {
        return this.repository.count();
    }

    /**
     * Returns Query Results that match filter ctriteria with the addition of being
     * able to page/sort the results. Instead of returning a Page<E>, this method
     * returns a List<E> to match other methods in this library.
     *
     * @param pageable contains page and sorting information
     * @param filters  List of query Filters
     * @return List of entities of type T
     */
    @Override
    public Page<E> getQueryResultPage(Criteria criteria) {
        return this.repository.findAll(bySearchCriteria(criteria), getPageRequest(criteria));
    }

    @Override
    public List<E> getQueryResult(Criteria criteria) {
        return this.repository.findAll(bySearchCriteria(criteria));
    }

    /**
     * Checks if an entity with the provided ID exists in the repository
     *
     * @param id entity identifier
     * @return boolean true if exist
     */
    @Override
    public boolean existsById(Long id) {
        return this.repository.existsById(id);
    }

    /**
     * Saves entity to the repository
     *
     * @param entity Entity of type E
     * @return returns entity with id
     */
    @Override
    public E save(E entity) {
        return this.repository.save(entity);
    }

    public <P> List<P> findBy(Criteria criteria, Class<P> clazz) {
        return this.repository.findBy(
                bySearchCriteria(criteria), q -> q.as(clazz).all());
    }

    /**
     * Returns the client defined EntityRepository back to the client
     *
     * @return repository of type R
     */
    @Override
    public R getRepository() {
        return this.repository;
    }

    private Specification<E> bySearchCriteria(Criteria criteria) {
        return filter(criteria).and(join(criteria));
    }

    private Specification<E> filter(Criteria criteria) {

        if (criteria.getFilters() == null || criteria.getFilters().isEmpty()) {
            return Specification.where(null);
        }

        Specification<E> specification =
                where(createSpecification(criteria.getFilters().remove(0)));
        for (Filter filter : criteria.getFilters()) {
            specification = specification.and(createSpecification(filter));
        }
        return specification;
    }

    private Specification<E> join(Criteria criteria) {

        if (criteria.getJoins() == null || criteria.getJoins().isEmpty()) {
            return Specification.where(null);
        }

        Specification<E> specification = with(criteria.getJoins().remove(0));
        for (JoinColumn joinColumn : criteria.getJoins()) {
            specification = with(joinColumn);
        }
        return specification;
    }

    public Specification<E> with(@Valid JoinColumn joinColumn) {
        return (root, query, criteriaBuilder) -> {
            Join<Object, Object> join = root.join(joinColumn.getJoin());
            BasicFilter filter = (BasicFilter) joinColumn.getFilter();
            return criteriaBuilder.equal(join.get(filter.getField()), filter.getValue());
        };
    }

    private Specification<E> createSpecification(Filter filter) {
        return this.filterHandlerFactory
                .getFilterHandler(filter.getFilterType())
                .handle(filter);
    }

    private PageRequest getPageRequest(Criteria criteria) {

        Sort sort = null;

        if (criteria.getOrder() == null) {
            sort = Sort.unsorted();
        } else {
            List<Sort.Order> desc = new ArrayList<>();

            if (criteria.getOrder().getDescending() != null
                    && !criteria.getOrder().getDescending().isEmpty()) {

                desc = criteria.getOrder().getDescending().stream()
                        .filter(Objects::nonNull)
                        .map(Sort.Order::desc)
                        .collect(Collectors.toList());
            }

            List<Sort.Order> asc = new ArrayList<>();

            if (criteria.getOrder().getAscending() != null
                    && !criteria.getOrder().getAscending().isEmpty()) {

                asc = criteria.getOrder().getAscending().stream()
                        .filter(Objects::nonNull)
                        .map(Sort.Order::asc)
                        .collect(Collectors.toList());
            }

            if (asc.isEmpty() && desc.isEmpty()) {
                sort = Sort.unsorted();
            } else if (!asc.isEmpty() && desc.isEmpty()) {
                sort = Sort.by(asc);
            } else if (asc.isEmpty() && !desc.isEmpty()) {
                sort = Sort.by(desc);
            } else if (asc.isEmpty() && desc.isEmpty()) {
                sort = Sort.by(Stream.concat(desc.stream(), asc.stream()).toList());
            }
        }

        return PageRequest.of(criteria.getPage(), criteria.getSize(), sort);
    }
}
