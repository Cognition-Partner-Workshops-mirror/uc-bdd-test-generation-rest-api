package fr.redfroggy.bdd.exceltestrunner.exception;

/**
 * Thrown when no matching step configuration is found in step-config.yml
 * for a given step description from the Excel file.
 *
 * @author Ashish,Raut
 */
public class StepConfigNotFoundException extends RuntimeException {

    // Serial version UID for serialization compatibility
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new StepConfigNotFoundException with the specified detail message.
     *
     * @param message the detail message describing which step description could not be matched
     */
    public StepConfigNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new StepConfigNotFoundException with the specified detail message and cause.
     *
     * @param message the detail message describing which step description could not be matched
     * @param cause   the underlying cause of this exception
     */
    public StepConfigNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
