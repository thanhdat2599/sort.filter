package vn.cnj.shared.sortfilter.enums.sort.filter;

import jakarta.persistence.criteria.*;
import vn.cnj.shared.sortfilter.request.SortRequest;

public enum SortDirection {
    ASC {
        public <T> Order build(Root<T> root, CriteriaBuilder cb, SortRequest request) {
            Path<?> path = getPathWithJoinTable(root, request);
            return cb.asc(path);
        }
    }, DESC {
        public <T> Order build(Root<T> root, CriteriaBuilder cb, SortRequest request) {
            Path<?> path = getPathWithJoinTable(root, request);
            return cb.desc(path);
        }
    };

    private static <T> Path<?> getPathWithJoinTable(Root<T> root, SortRequest request) {
        String[] relations = request.getKey().split("\\.");
        Path<?> path;
        if (relations.length <= 1) {
            path = root.get(request.getKey());
        } else {
            path = root.join(relations[0], JoinType.LEFT).get(relations[1]);
        }
        return path;
    }

    public abstract <T> Order build(Root<T> root, CriteriaBuilder cb, SortRequest request);

}
