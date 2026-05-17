package fr.redfroggy.bdd.exceltestrunner.config;

import java.util.Map;

/**
 * Configuration for response verification rules such as array-contains or status-check,
 * used to validate REST API responses.
 *
 * @author Ashish,Raut
 */
public class VerificationConfig {

    // Type of verification to perform (e.g., "status-check", "array-contains", "json-path-equals")
    private String type;

    // Expected value for the verification (e.g., "200" for status check)
    private String expectedValue;

    // JSON path expression to evaluate against the response body
    private String jsonPath;

    // Additional parameters for complex verification rules
    private Map<String, String> parameters;

    /**
     * Default no-arg constructor.
     */
    public VerificationConfig() {
    }

    /**
     * Constructs a VerificationConfig with the specified type and expected value.
     *
     * @param type          the verification type (e.g., "status-check", "array-contains")
     * @param expectedValue the expected value to match against
     */
    public VerificationConfig(String type, String expectedValue) {
        this.type = type;
        this.expectedValue = expectedValue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public void setExpectedValue(String expectedValue) {
        this.expectedValue = expectedValue;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
