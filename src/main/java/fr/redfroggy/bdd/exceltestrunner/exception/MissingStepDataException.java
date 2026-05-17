package fr.redfroggy.bdd.exceltestrunner.exception;

/**
 * Thrown when a step's Data column is missing required fields as defined
 * in the step configuration.
 *
 * @author Ashish,Raut
 */
public class MissingStepDataException extends RuntimeException {

    // Serial version UID for serialization compatibility
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new MissingStepDataException with the specified detail message.
     *
     * @param message the detail message describing which required data fields are missing
     */
    public MissingStepDataException(String message) {
        super(message);
    }

    /**
     * Constructs a new MissingStepDataException with the specified detail message and cause.
     *
     * @param message the detail message describing which required data fields are missing
     * @param cause   the underlying cause of this exception
     */
    public MissingStepDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
