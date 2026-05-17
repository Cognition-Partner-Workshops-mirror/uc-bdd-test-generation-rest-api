package fr.redfroggy.bdd.exceltestrunner.config;

import java.util.List;

/**
 * POJO representing a single step configuration entry from step-config.yml,
 * including HTTP method, endpoint, JSON template, and data field mappings.
 *
 * @author Ashish,Raut
 */
public class StepConfig {

    // Regex pattern to match step descriptions from the Excel file
    private String stepPattern;

    // HTTP method to use for the REST call (GET, POST, PUT, DELETE, PATCH)
    private String httpMethod;

    // API endpoint URL pattern with optional placeholders (e.g., "/api/users/{userId}")
    private String endpoint;

    // JSON body template with placeholder tokens for data substitution
    private String jsonTemplate;

    // List of required data field names that must be present in the Excel Data column
    private List<String> requiredFields;

    // List of verification rules to apply to the API response
    private List<VerificationConfig> verifications;

    /**
     * Default no-arg constructor.
     */
    public StepConfig() {
    }

    public String getStepPattern() {
        return stepPattern;
    }

    public void setStepPattern(String stepPattern) {
        this.stepPattern = stepPattern;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getJsonTemplate() {
        return jsonTemplate;
    }

    public void setJsonTemplate(String jsonTemplate) {
        this.jsonTemplate = jsonTemplate;
    }

    public List<String> getRequiredFields() {
        return requiredFields;
    }

    public void setRequiredFields(List<String> requiredFields) {
        this.requiredFields = requiredFields;
    }

    public List<VerificationConfig> getVerifications() {
        return verifications;
    }

    public void setVerifications(List<VerificationConfig> verifications) {
        this.verifications = verifications;
    }
}
