package vn.cnj.shared.sortfilter.exception;

/**
 * Exception thrown when a filter operation is invalid
 */
public class InvalidFilterException extends SortFilterException {
    
    public InvalidFilterException(String message) {
        super(message);
    }
    
    public InvalidFilterException(String message, Throwable cause) {
        super(message, cause);
    }
} 