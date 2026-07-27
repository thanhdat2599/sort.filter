package vn.cnj.shared.sortfilter.exception;

/**
 * Base exception class for sort filter operations
 */
public class SortFilterException extends RuntimeException {
    
    public SortFilterException(String message) {
        super(message);
    }
    
    public SortFilterException(String message, Throwable cause) {
        super(message, cause);
    }
} 