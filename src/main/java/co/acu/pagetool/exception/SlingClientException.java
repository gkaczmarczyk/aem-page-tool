package co.acu.pagetool.exception;

/**
 * Custom exception for SlingClient operations.
 */
public class SlingClientException extends Exception {
    public SlingClientException(String message) {
        super(message);
    }

    public SlingClientException(String message, Throwable cause) {
        super(message, cause);
    }

}
