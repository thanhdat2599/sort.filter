package vn.cnj.sort.filter.specification;

import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import vn.cnj.sort.filter.request.FilterRequest;
import vn.cnj.sort.filter.request.SearchRequest;
import vn.cnj.sort.filter.request.SortRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@AllArgsConstructor
public class SearchSpecification<T> implements Specification<T> {

    private final transient SearchRequest request;

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate predicate;
        if ("search_or".equals(this.request.getSearchType())) {
            predicate = cb.disjunction();
        } else {
            predicate = cb.equal(cb.literal(Boolean.TRUE), Boolean.TRUE);
        }
        for (FilterRequest filter : this.request.getFilters()) {
            log.info("Filter: {} {} {}", filter.getKey(), filter.getOperator().toString(), filter.getValue());
            log.info("Filter IN: {} {} {}", filter.getKey(), filter.getOperator().toString(), filter.getValues());
            predicate = filter.getOperator().build(root, cb, filter, predicate);
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