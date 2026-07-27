package vn.cnj.shared.sortfilter.specification;

import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import vn.cnj.shared.sortfilter.exception.InvalidSearchException;
import vn.cnj.shared.sortfilter.request.FilterRequest;
import vn.cnj.shared.sortfilter.request.SearchRequest;
import vn.cnj.shared.sortfilter.request.SortRequest;
import vn.cnj.shared.sortfilter.utils.CacheUtils;
import vn.cnj.shared.sortfilter.utils.ValidationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Specification for searching, filtering and sorting entities
 *
 * @param <T> the entity type
 */
@Slf4j
@AllArgsConstructor
public class SearchSpecification<T> implements Specification<T> {

    public static final String SEARCH_OR = "search_or";
    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final int MAX_PAGE_SIZE = 1000;
    
    private final transient SearchRequest request;

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        try {
            validateRequest();
            
            Predicate predicate = createBasePredicate(cb);
            predicate = applyFilters(root, cb, predicate);
            applySorting(root, query, cb);
            
            return predicate;
        } catch (Exception e) {
            log.error("Error building search specification", e);
            throw new InvalidSearchException("Error building search specification: " + e.getMessage(), e);
        }
    }

    /**
     * Validates the search request
     *
     * @throws InvalidSearchException if the request is invalid
     */
    private void validateRequest() {
        ValidationUtils.validateNotNull(request, "Search request cannot be null");
        if (request.getFilters() == null) {
            request.setFilters(new ArrayList<>());
        }
        if (request.getSorts() == null) {
            request.setSorts(new ArrayList<>());
        }
    }

    /**
     * Creates the base predicate based on search type
     *
     * @param cb the criteria builder
     * @return the base predicate
     */
    private Predicate createBasePredicate(CriteriaBuilder cb) {
        return SEARCH_OR.equals(request.getSearchType()) 
            ? cb.disjunction() 
            : cb.conjunction();
    }

    /**
     * Applies filters to the predicate
     *
     * @param root the root entity
     * @param cb the criteria builder
     * @param predicate the current predicate
     * @return the updated predicate
     */
    private Predicate applyFilters(Root<T> root, CriteriaBuilder cb, Predicate predicate) {
        if (SEARCH_OR.equals(request.getSearchType())) {
            return applyOrFilters(root, cb);
        }
        
        for (FilterRequest filter : request.getFilters()) {
            log.debug("Applying filter: {} {} {}", filter.getKey(), filter.getOperator(), filter.getValue());
            predicate = filter.getOperator().build(root, cb, filter, predicate);
        }
        return predicate;
    }

    /**
     * Applies OR filters with main filters
     *
     * @param root the root entity
     * @param cb the criteria builder
     * @return the combined predicate
     */
    private Predicate applyOrFilters(Root<T> root, CriteriaBuilder cb) {
        List<FilterRequest> mainFilters = request.getFilters().stream()
            .filter(filter -> !filter.getOperator().name().endsWith("_OR"))
            .collect(Collectors.toList());
            
        List<FilterRequest> orFilters = request.getFilters().stream()
            .filter(filter -> filter.getOperator().name().endsWith("_OR"))
            .collect(Collectors.toList());

        Predicate mainPredicate = buildPredicateList(root, cb, mainFilters, cb.conjunction());
        Predicate orPredicate = buildPredicateList(root, cb, orFilters, cb.disjunction());
        
        return cb.and(mainPredicate, orPredicate);
    }

    /**
     * Builds a predicate from a list of filters
     *
     * @param root the root entity
     * @param cb the criteria builder
     * @param filters the list of filters
     * @param initialPredicate the initial predicate
     * @return the combined predicate
     */
    private Predicate buildPredicateList(Root<T> root, CriteriaBuilder cb, List<FilterRequest> filters, Predicate initialPredicate) {
        if (CollectionUtils.isEmpty(filters)) {
            return initialPredicate;
        }
        
        Predicate predicate = initialPredicate;
        for (FilterRequest filter : filters) {
            log.debug("Applying filter: {} {} {}", filter.getKey(), filter.getOperator(), filter.getValue());
            predicate = filter.getOperator().build(root, cb, filter, predicate);
        }
        return predicate;
    }

    /**
     * Applies sorting to the query
     *
     * @param root the root entity
     * @param query the criteria query
     * @param cb the criteria builder
     */
    private void applySorting(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Order> orders = new ArrayList<>();
        for (SortRequest sort : request.getSorts()) {
            log.debug("Applying sort: {} {}", sort.getKey(), sort.getDirection());
            orders.add(sort.getDirection().build(root, cb, sort));
        }
        query.orderBy(orders);
    }

    /**
     * Gets a pageable object with validation
     *
     * @param page the page number
     * @param size the page size
     * @return the pageable object
     */
    public static Pageable getPageable(Integer page, Integer size) {
        int validatedPage = Objects.requireNonNullElse(page, 0);
        int validatedSize = validatePageSize(Objects.requireNonNullElse(size, DEFAULT_PAGE_SIZE));
        
        String cacheKey = "pageable:" + validatedPage + ":" + validatedSize;
        Optional<Pageable> cachedPageable = CacheUtils.getFromCache(CacheUtils.getCacheManager(), cacheKey);
        
        if (cachedPageable.isPresent()) {
            return cachedPageable.get();
        }
        
        Pageable pageable = PageRequest.of(validatedPage, validatedSize);
        CacheUtils.putInCache(CacheUtils.getCacheManager(), cacheKey, pageable);
        return pageable;
    }

    /**
     * Validates and adjusts the page size
     *
     * @param size the requested page size
     * @return the validated page size
     */
    private static int validatePageSize(int size) {
        if (size <= 0) {
            log.warn("Invalid page size: {}. Using default: {}", size, DEFAULT_PAGE_SIZE);
            return DEFAULT_PAGE_SIZE;
        }
        if (size > MAX_PAGE_SIZE) {
            log.warn("Page size {} exceeds maximum {}. Using maximum.", size, MAX_PAGE_SIZE);
            return MAX_PAGE_SIZE;
        }
        return size;
    }
}