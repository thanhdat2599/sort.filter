package vn.cnj.shared.sortfilter.enums.sort.filter;

import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import vn.cnj.shared.sortfilter.exception.InvalidFilterException;
import vn.cnj.shared.sortfilter.request.FilterRequest;
import vn.cnj.shared.sortfilter.utils.ValidationUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public enum Operator {

    EQUAL {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            Object value = parseValue(request);
            Expression<?> key = getExpressionWithJoining(root, request);
            return cb.and(cb.equal(key, value), predicate);
        }
    },

    EQUAL_OR {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            Object value = parseValue(request);
            Expression<?> key = getExpressionWithJoining(root, request);
            return cb.or(predicate, cb.equal(key, value));
        }
    },

    GTE_OR {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            return buildNumericPredicate(root, cb, request, predicate, (path, value) -> cb.greaterThanOrEqualTo(path, value), true);
        }
    },

    LTE_OR {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            return buildNumericPredicate(root, cb, request, predicate, (path, value) -> cb.lessThanOrEqualTo(path, value), true);
        }
    },

    GT {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            return buildNumericPredicate(root, cb, request, predicate, (path, value) -> cb.greaterThan(path, value), false);
        }
    },

    GTE {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            return buildNumericPredicate(root, cb, request, predicate, (path, value) -> cb.greaterThanOrEqualTo(path, value), false);
        }
    },

    GT_OR {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            return buildNumericPredicate(root, cb, request, predicate, (path, value) -> cb.greaterThan(path, value), true);
        }
    },

    LT {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            return buildNumericPredicate(root, cb, request, predicate, (path, value) -> cb.lessThan(path, value), false);
        }
    },

    LTE {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            return buildNumericPredicate(root, cb, request, predicate, (path, value) -> cb.lessThanOrEqualTo(path, value), false);
        }
    },

    LT_OR {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            return buildNumericPredicate(root, cb, request, predicate, (path, value) -> cb.lessThan(path, value), true);
        }
    },

    NOT_EQUAL {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            Object value = parseValue(request);
            Expression<?> key = getExpressionWithJoining(root, request);
            return cb.and(cb.notEqual(key, value), predicate);
        }
    },

    @SuppressWarnings("unchecked")
    LIKE {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            Expression<String> key = (Expression<String>) getExpressionWithJoining(root, request);
            return cb.and(cb.like(cb.upper(key), "%" + request.getValue().toString().toUpperCase() + "%"), predicate);
        }
    },

    @SuppressWarnings("unchecked")
    LIKE_OR {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            Expression<String> key = (Expression<String>) getExpressionWithJoining(root, request);
            return cb.or(predicate, cb.like(cb.upper(key), "%" + request.getValue().toString().toUpperCase() + "%"));
        }
    },

    @SuppressWarnings("unchecked")
    IN {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            Expression<String> key = (Expression<String>) getExpressionWithJoining(root, request);
            CriteriaBuilder.In<Object> inClause = cb.in(key);

            List<Object> values = request.getValues();
            ValidationUtils.validateNotNull(values, "Values cannot be null for IN operation");
            
            for (Object value : values) {
                inClause.value(parseValue(request, value));
            }
            return cb.and(inClause, predicate);
        }
    },

    BETWEEN {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            Object value = parseValue(request);
            Object valueTo = parseValue(request, request.getValueTo());
            
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

            log.warn("Cannot use between for {} field type", request.getFieldType());
            return predicate;
        }
    },

    BETWEEN_OR {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            Object value = request.getFieldType().parse(request.getValue().toString());
            Object valueTo = request.getFieldType().parse(request.getValueTo().toString());
            if (request.getFieldType() == FieldType.DATE) {
                LocalDateTime startDate = (LocalDateTime) value;
                LocalDateTime endDate = (LocalDateTime) valueTo;
                Expression<LocalDateTime> key = root.get(request.getKey());
                return cb.or(predicate, cb.and(cb.greaterThanOrEqualTo(key, startDate), cb.lessThanOrEqualTo(key, endDate)));
            }

            if (request.getFieldType() != FieldType.CHAR && request.getFieldType() != FieldType.BOOLEAN) {
                Number start = (Number) value;
                Number end = (Number) valueTo;
                Expression<Number> key = root.get(request.getKey());
                return cb.or(predicate, cb.and(cb.ge(key, start), cb.le(key, end)));
            }

            log.info("Can not use between for {} field type.", request.getFieldType());
            return predicate;
        }
    };

    private static final Map<String, Object> valueCache = new ConcurrentHashMap<>();

    private static Object parseValue(FilterRequest request) {
        return parseValue(request, request.getValue());
    }

    private static Object parseValue(FilterRequest request, Object value) {
        String cacheKey = request.getFieldType() + ":" + value;
        return valueCache.computeIfAbsent(cacheKey, k -> {
            try {
                return request.getFieldType().parse(value.toString());
            } catch (Exception e) {
                log.error("Error parsing value: {}", value, e);
                throw new InvalidFilterException("Invalid value format: " + value);
            }
        });
    }

    private static <T, N extends Number> Predicate buildNumericPredicate(
            Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate,
            NumericPredicateBuilder<N> builder, boolean isOr) {
        
        Object value = parseValue(request);
        String[] relations = request.getKey().split("\\.");
        Path<?> path = getPath(root, request, relations);

        Class<?> type = path.getJavaType();
        if (type.equals(Integer.class)) {
            return buildPredicate(cb, path.as(Integer.class), (Integer) value, predicate, builder, isOr);
        } else if (type.equals(Long.class)) {
            return buildPredicate(cb, path.as(Long.class), (Long) value, predicate, builder, isOr);
        } else if (type.equals(Float.class)) {
            return buildPredicate(cb, path.as(Float.class), (Float) value, predicate, builder, isOr);
        } else if (type.equals(Double.class)) {
            return buildPredicate(cb, path.as(Double.class), (Double) value, predicate, builder, isOr);
        } else if (type.equals(BigDecimal.class)) {
            return buildPredicate(cb, path.as(BigDecimal.class), (BigDecimal) value, predicate, builder, isOr);
        }
        throw new InvalidFilterException("Unsupported number type: " + type);
    }

    private static <T, N extends Number> Predicate buildPredicate(
            CriteriaBuilder cb, Expression<N> path, N value, Predicate predicate,
            NumericPredicateBuilder<N> builder, boolean isOr) {
        
        Predicate newPredicate = builder.build(path, value);
        return isOr ? cb.or(predicate, newPredicate) : cb.and(newPredicate, predicate);
    }

    private static <T> Path<?> getPath(Root<T> root, FilterRequest request, String[] relations) {
        ValidationUtils.validateNotEmpty(request.getKey(), "Key cannot be empty");
        if (relations.length <= 1) {
            return root.get(request.getKey());
        }
        return root.join(relations[0]).get(relations[1]);
    }

    private static <T> Expression<?> getExpressionWithJoining(Root<T> root, FilterRequest request) {
        String[] relations = request.getKey().split("\\.");
        if (relations.length <= 1) {
            return root.get(request.getKey());
        }
        return root.join(relations[0]).get(relations[1]);
    }

    @FunctionalInterface
    private interface NumericPredicateBuilder<N extends Number> {
        Predicate build(Expression<N> path, N value);
    }

    public abstract <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate);
}
