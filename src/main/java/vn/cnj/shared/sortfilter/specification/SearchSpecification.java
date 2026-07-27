package vn.cnj.shared.sortfilter.specification;

import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import vn.cnj.shared.sortfilter.request.FilterRequest;
import vn.cnj.shared.sortfilter.request.SearchRequest;
import vn.cnj.shared.sortfilter.request.SortRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@AllArgsConstructor
public class SearchSpecification<T> implements Specification<T> {

    public static final String SEARCH_OR = "search_or";
    private final transient SearchRequest request;

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate predicate;
        if (SEARCH_OR.equals(this.request.getSearchType())) {
            predicate = cb.disjunction();
        } else {
            predicate = cb.equal(cb.literal(Boolean.TRUE), Boolean.TRUE);
        }
        if (SEARCH_OR.equals(this.request.getSearchType())) {
            List<FilterRequest> mainFilter = this.request.getFilters().stream().filter(s -> !s.getOperator().name().endsWith("_OR")).toList();
            List<FilterRequest> orFilter = this.request.getFilters().stream().filter(s -> s.getOperator().name().endsWith("_OR")).toList();
            Predicate mainPredicate = cb.conjunction();
            if (!CollectionUtils.isEmpty(mainFilter)) {
                for (FilterRequest filter : mainFilter) {
                    mainPredicate = filter.getOperator().build(root, cb, filter, mainPredicate);
                }
            }

            Predicate orPredicate = cb.disjunction();
            if (!CollectionUtils.isEmpty(orFilter)) {
                for (FilterRequest filter : orFilter) {
                    orPredicate = filter.getOperator().build(root, cb, filter, orPredicate);
                }
            }
            predicate = cb.and(mainPredicate, orPredicate);
        } else {
            for (FilterRequest filter : this.request.getFilters()) {
                log.info("Filter: {} {} {}", filter.getKey(), filter.getOperator().toString(), filter.getValue());
                log.info("Filter IN: {} {} {}", filter.getKey(), filter.getOperator().toString(), filter.getValues());
                predicate = filter.getOperator().build(root, cb, filter, predicate);
            }
        }

        List<Order> orders = new ArrayList<>();
        for (SortRequest sort : this.request.getSorts()) {
            orders.add(sort.getDirection().build(root, cb, sort));
        }

        query.orderBy(orders);
        return predicate;
    }

    public static Pageable getPageable(Integer page, Integer size) {
        return PageRequest.of(Objects.requireNonNullElse(page, 0), Objects.requireNonNullElse(size, 100));
    }

}