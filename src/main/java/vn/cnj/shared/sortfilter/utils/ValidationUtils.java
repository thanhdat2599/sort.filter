package vn.cnj.shared.sortfilter.utils;

import vn.cnj.shared.sortfilter.exception.InvalidFilterException;
import vn.cnj.shared.sortfilter.exception.InvalidSortException;

/**
 * Utility class for validation operations
 */
public final class ValidationUtils {
    
    private ValidationUtils() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Validates that the given object is not null
     *
     * @param object the object to validate
     * @param message the error message
     * @throws InvalidFilterException if the object is null
     */
    public static void validateNotNull(Object object, String message) {
        if (object == null) {
            throw new InvalidFilterException(message);
        }
    }
    
    /**
     * Validates that the given string is not empty
     *
     * @param string the string to validate
     * @param message the error message
     * @throws InvalidFilterException if the string is empty
     */
    public static void validateNotEmpty(String string, String message) {
        if (string == null || string.trim().isEmpty()) {
            throw new InvalidFilterException(message);
        }
    }
    
    /**
     * Validates that the given sort field is valid
     *
     * @param field the field to validate
     * @param message the error message
     * @throws InvalidSortException if the field is invalid
     */
    public static void validateSortField(String field, String message) {
        validateNotEmpty(field, message);
    }
} 