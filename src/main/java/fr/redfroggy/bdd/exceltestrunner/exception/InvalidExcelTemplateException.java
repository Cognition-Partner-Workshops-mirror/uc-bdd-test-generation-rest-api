package fr.redfroggy.bdd.exceltestrunner.exception;

/**
 * Thrown when the uploaded Excel file does not match the expected column template
 * (TC#, Title, Description, Steps, Data, etc.).
 *
 * @author Ashish,Raut
 */
public class InvalidExcelTemplateException extends RuntimeException {

    // Serial version UID for serialization compatibility
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new InvalidExcelTemplateException with the specified detail message.
     *
     * @param message the detail message describing which columns are invalid or missing
     */
    public InvalidExcelTemplateException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidExcelTemplateException with the specified detail message and cause.
     *
     * @param message the detail message describing which columns are invalid or missing
     * @param cause   the underlying cause of this exception
     */
    public InvalidExcelTemplateException(String message, Throwable cause) {
        super(message, cause);
    }
}
