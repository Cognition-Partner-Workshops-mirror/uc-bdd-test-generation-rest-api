package fr.redfroggy.bdd.exceltestrunner.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Model representing a single executable step within a test case,
 * including step description, input data, execution status, and timing.
 *
 * @author Ashish,Raut
 */
public class TestStep {

    // Step description text from the Excel file (e.g., "Create a new user")
    private String description;

    // Parsed data fields from the Excel Data column as key-value pairs
    private Map<String, String> data;

    // Execution status of this step: PASS, FAIL, SKIP, or PENDING
    private String status;

    // Error message captured if the step execution fails
    private String errorMessage;

    // Execution duration in milliseconds
    private long durationMs;

    // HTTP status code returned by the API call
    private int responseStatusCode;

    // Response body returned by the API call
    private String responseBody;

    /**
     * Default constructor initializing data map and pending status.
     */
    public TestStep() {
        this.data = new HashMap<>();
        this.status = "PENDING";
    }

    /**
     * Constructs a TestStep with the specified description and data.
     *
     * @param description the step description from the Excel file
     * @param data        the parsed key-value data for this step
     */
    public TestStep(String description, Map<String, String> data) {
        this.description = description;
        this.data = data != null ? data : new HashMap<>();
        this.status = "PENDING";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public int getResponseStatusCode() {
        return responseStatusCode;
    }

    public void setResponseStatusCode(int responseStatusCode) {
        this.responseStatusCode = responseStatusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }
}
