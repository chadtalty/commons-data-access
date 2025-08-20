package com.chadtalty.commons.data.access;

import com.chadtalty.commons.data.access.filter.handler.FilterHandlerFactory;
import com.chadtalty.commons.data.access.repository.EntityRepository;
import com.chadtalty.commons.data.query.BasicFilter;
import com.chadtalty.commons.data.query.Criteria;
import com.chadtalty.commons.data.query.Filter;
import com.chadtalty.commons.data.query.JoinSpec;
import com.chadtalty.commons.data.query.PageableCriteria;
import com.chadtalty.commons.data.query.SortSpec;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

/**
 * Base DAO implementation providing CRUD and specification-based querying.
 *
 * <p>Joins are applied as simple equality filters (see {@link #createJoinSpecification(JoinSpec)}).
 * For more advanced join conditions, consider extending this method or mapping joins to filter
 * handlers that operate within join scopes.
 *
 * @param <E> entity type
 * @param <R> repository type (must extend {@link EntityRepository})
 */
public abstract class AbstractDAO<E, R extends EntityRepository<E, Long>> implements DAO<E, R> {

    @Autowired
    protected R repository;

    @Autowired
    private FilterHandlerFactory<E> filterHandlerFactory;

    @Override
    public E findById(long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Entity not found: " + id));
    }

    @Override
    public List<E> findAll() {
        return repository.findAll();
    }

    @Override
    public Page<E> findAll(org.springframework.data.domain.Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Long count() {
        return repository.count();
    }

    @Override
    public Page<E> getQueryResultPage(PageableCriteria criteria) {
        return repository.findAll(buildSearchSpecification(criteria), getPageRequest(criteria));
    }

    @Override
    public List<E> getQueryResult(Criteria criteria) {
        return repository.findAll(buildSearchSpecification(criteria));
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public E save(E entity) {
        return repository.save(entity);
    }

    @Override
    public R getRepository() {
        return repository;
    }

    // -- Internals ------------------------------------------------------------------------------

    /**
     * Builds a combined specification from filters and joins (AND-ed together).
     */
    private Specification<E> buildSearchSpecification(Criteria criteria) {
        return Specification.where(applyFilters(criteria.getFilters())).and(applyJoins(criteria.getJoins()));
    }

    /**
     * AND-combines specifications generated from each filter.
     */
    private Specification<E> applyFilters(List<Filter> filters) {
        return Optional.ofNullable(filters)
                .map(f -> f.stream()
                        .map(this::createSpecification)
                        .reduce(Specification::and)
                        .orElse(null))
                .orElse(null);
    }

    /**
     * AND-combines specifications generated from each join specification.
     */
    private Specification<E> applyJoins(List<JoinSpec> joins) {
        return Optional.ofNullable(joins)
                .map(j -> j.stream()
                        .map(this::createJoinSpecification)
                        .reduce(Specification::and)
                        .orElse(null))
                .orElse(null);
    }

    /**
     * Creates a simple join specification which performs a join and applies an equality constraint
     * on the joined attribute.
     *
     * <p>Note: This implementation currently supports only {@link BasicFilter} equality on the
     * joined path. Extend as needed for more complex logic.
     */
    private Specification<E> createJoinSpecification(JoinSpec joinSpec) {
        return (root, query, criteriaBuilder) -> {
            Join<Object, Object> join = root.join(joinSpec.getJoin());
            BasicFilter filter = (BasicFilter) joinSpec.getFilter();
            return criteriaBuilder.equal(join.get(filter.getField()), filter.getValue());
        };
    }

    /**
     * Delegates a filter to its handler via {@link FilterHandlerFactory}.
     */
    private Specification<E> createSpecification(Filter filter) {
        return filterHandlerFactory.getFilterHandler(filter.getType()).handle(filter);
    }

    /**
     * Builds a {@link PageRequest} using {@link PageableCriteria#getPage()}, {@link
     * PageableCriteria#getSize()}, and optional {@link SortSpec}.
     */
    private PageRequest getPageRequest(PageableCriteria criteria) {
        List<Sort.Order> orders = Stream.concat(
                        Optional.ofNullable(criteria.getSort()).map(SortSpec::getAscending).stream()
                                .flatMap(Collection::stream)
                                .map(Sort.Order::asc),
                        Optional.ofNullable(criteria.getSort()).map(SortSpec::getDescending).stream()
                                .flatMap(Collection::stream)
                                .map(Sort.Order::desc))
                .collect(Collectors.toList());

        Sort sort = orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
        return PageRequest.of(criteria.getPage(), criteria.getSize(), sort);
    }
}
