package vn.cnj.sort.filter.enums.sort.filter;

import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import vn.cnj.sort.filter.request.FilterRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public enum Operator {

    EQUAL {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            Object value = request.getFieldType().parse(request.getValue().toString());
            Expression<?> key = getExpressionWithJoining(root, request);
            return cb.and(cb.equal(key, value), predicate);
        }
    },
    EQUAL_OR {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            Object value = request.getFieldType().parse(request.getValue().toString());
            Expression<?> key = getExpressionWithJoining(root, request);
            return cb.or(cb.equal(key, value), predicate);
        }
    },

    //greater than or equals Or
    GTE_OR {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            Object value = request.getFieldType().parse(request.getValue().toString());
//            if (!CommonUtils.isNumber(value)) {
//                throw new RuntimeException("value: " + value + " should be number");
//            }
            String[] relations = request.getKey().split("\\.");
            Path<?> path;
            if (relations.length <= 1) {
                path = root.get(request.getKey());
            } else {
                path = root.join(relations[0]).get(relations[1]);
            }

            if (path.getJavaType().equals(Integer.class)) {
                return cb.or(cb.greaterThanOrEqualTo(path.as(Integer.class), (Integer) value), predicate);
            }

            if (path.getJavaType().equals(Long.class)) {
                return cb.or(cb.greaterThanOrEqualTo(path.as(Long.class), (Long) value), predicate);
            }

            if (path.getJavaType().equals(Float.class)) {
                return cb.or(cb.greaterThanOrEqualTo(path.as(Float.class), (Float) value), predicate);
            }

            if (path.getJavaType().equals(Double.class)) {
                return cb.or(cb.greaterThanOrEqualTo(path.as(Double.class), (Double) value), predicate);
            }
            if (path.getJavaType().equals(BigDecimal.class)) {
                return cb.or(cb.greaterThanOrEqualTo(path.as(BigDecimal.class), (BigDecimal) value), predicate);
            }
            throw new RuntimeException("Unsupported number type for GT operation");
        }
    },

    NOT_EQUAL {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            Object value = request.getFieldType().parse(request.getValue().toString());
            Expression<?> key = getExpressionWithJoining(root, request);
            return cb.and(cb.notEqual(key, value), predicate);
        }
    },

    LIKE {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            Expression<String> key = (Expression<String>) getExpressionWithJoining(root, request);
            return cb.and(cb.like(cb.upper(key), "%" + request.getValue().toString().toUpperCase() + "%"), predicate);
        }
    },

    LIKE_OR {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            Expression<String> key = (Expression<String>) getExpressionWithJoining(root, request);
            return cb.or(predicate, cb.like(cb.upper(key), "%" + request.getValue().toString().toUpperCase() + "%"));
        }
    },

    IN {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            String[] relations = request.getKey().split("\\.");
            Expression<String> key = (Expression<String>) getExpressionWithJoining(root, request);
            CriteriaBuilder.In<Object> inClause = cb.in(key);

            List<Object> values = request.getValues();
            for (Object value : values) {
                inClause.value(request.getFieldType().parse(value.toString()));
            }
            return cb.and(inClause, predicate);
        }
    },

    BETWEEN {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            Object value = request.getFieldType().parse(request.getValue().toString());
            Object valueTo = request.getFieldType().parse(request.getValueTo().toString());
            if (request.getFieldType() == FieldType.DATE) {
                LocalDateTime startDate = (LocalDateTime) value;
                LocalDateTime endDate = (LocalDateTime) valueTo;
                Expression<LocalDateTime> key = root.get(request.getKey());
                return cb.and(cb.and(cb.greaterThanOrEqualTo(key, startDate), cb.lessThanOrEqualTo(key, endDate)), predicate);
            }

            if (request.getFieldType() != FieldType.CHAR && request.getFieldType() != FieldType.BOOLEAN) {
                Number start = (Number) value;
                Number end = (Number) valueTo;
                Expression<Number> key = root.get(request.getKey());
                return cb.and(cb.and(cb.ge(key, start), cb.le(key, end)), predicate);
            }

            log.info("Can not use between for {} field type.", request.getFieldType());
            return predicate;
        }
    };

    private static <T> Expression<?> getExpressionWithJoining(Root<T> root, FilterRequest request) {
        String[] relations = request.getKey().split("\\.");
        if (relations.length <= 1) {
            return root.get(request.getKey());
        }
        return root.join(relations[0]).get(relations[1]);
    }

    public abstract <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate);
}
