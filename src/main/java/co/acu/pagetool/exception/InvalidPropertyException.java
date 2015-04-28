package co.acu.pagetool.exception;

/**
 *
 * @author Greg Kaczmarczyk
 */
public class InvalidPropertyException extends Exception {

    public InvalidPropertyException() {
    }

    public InvalidPropertyException(String message) {
        super(message);
    }

    public InvalidPropertyException(Throwable cause) {
        super(cause);
    }

    public InvalidPropertyException(String message, Throwable cause) {
        super(message, cause);
    }

}
