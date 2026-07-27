package vn.cnj.shared.sortfilter.exception;

/**
 * Exception thrown when a sort operation is invalid
 */
public class InvalidSortException extends SortFilterException {
    
    public InvalidSortException(String message) {
        super(message);
    }
    
    public InvalidSortException(String message, Throwable cause) {
        super(message, cause);
    }
} 