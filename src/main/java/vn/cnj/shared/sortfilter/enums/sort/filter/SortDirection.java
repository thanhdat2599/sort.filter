package vn.cnj.shared.sortfilter.enums.sort.filter;

import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import vn.cnj.shared.sortfilter.exception.InvalidSortException;
import vn.cnj.shared.sortfilter.request.SortRequest;
import vn.cnj.shared.sortfilter.utils.ValidationUtils;

/**
 * Enum representing sort directions (ASCENDING and DESCENDING)
 */
@Slf4j
public enum SortDirection {
    ASC {
        @Override
        public <T> Order build(Root<T> root, CriteriaBuilder cb, SortRequest request) {
            Path<?> path = getPathWithJoinTable(root, request);
            return cb.asc(path);
        }
    }, 
    
    DESC {
        @Override
        public <T> Order build(Root<T> root, CriteriaBuilder cb, SortRequest request) {
            Path<?> path = getPathWithJoinTable(root, request);
            return cb.desc(path);
        }
    };

    /**
     * Gets the path for sorting, handling both direct fields and joined fields
     *
     * @param root the root entity
     * @param request the sort request
     * @param <T> the entity type
     * @return the path to sort by
     * @throws InvalidSortException if the key is invalid or the join fails
     */
    private static <T> Path<?> getPathWithJoinTable(Root<T> root, SortRequest request) {
        try {
            ValidationUtils.validateNotEmpty(request.getKey(), "Sort key cannot be empty");
            
            String[] relations = request.getKey().split("\\.");
            if (relations.length <= 1) {
                return root.get(request.getKey());
            }
            
            // Handle nested joins if needed
            Join<?, ?> join = root.join(relations[0], JoinType.LEFT);
            for (int i = 1; i < relations.length - 1; i++) {
                join = join.join(relations[i], JoinType.LEFT);
            }
            return join.get(relations[relations.length - 1]);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid sort path: {}", request.getKey(), e);
            throw new InvalidSortException("Invalid sort path: " + request.getKey(), e);
        } catch (Exception e) {
            log.error("Error creating sort path for key: {}", request.getKey(), e);
            throw new InvalidSortException("Error creating sort path: " + request.getKey(), e);
        }
    }

    /**
     * Builds the sort order for the given request
     *
     * @param root the root entity
     * @param cb the criteria builder
     * @param request the sort request
     * @param <T> the entity type
     * @return the sort order
     */
    public abstract <T> Order build(Root<T> root, CriteriaBuilder cb, SortRequest request);
}
