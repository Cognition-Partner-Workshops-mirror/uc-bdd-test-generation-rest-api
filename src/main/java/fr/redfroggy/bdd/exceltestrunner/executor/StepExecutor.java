package fr.redfroggy.bdd.exceltestrunner.executor;

import com.jayway.jsonpath.JsonPath;
import fr.redfroggy.bdd.exceltestrunner.config.StepConfig;
import fr.redfroggy.bdd.exceltestrunner.config.StepConfigLoader;
import fr.redfroggy.bdd.exceltestrunner.config.VerificationConfig;
import fr.redfroggy.bdd.exceltestrunner.exception.MissingStepDataException;
import fr.redfroggy.bdd.exceltestrunner.model.TestStep;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Executes a single test step by resolving its configuration, building the REST request,
 * invoking the API, and validating the response.
 *
 * @author Ashish,Raut
 */
@Component
public class StepExecutor {

    // Loader for step configuration from step-config.yml
    private final StepConfigLoader stepConfigLoader;

    // Spring RestTemplate for making HTTP requests
    private final RestTemplate restTemplate;

    /**
     * Constructs a StepExecutor with the required dependencies.
     *
     * @param stepConfigLoader the loader for step configuration
     * @param restTemplate     the RestTemplate for HTTP calls
     */
    public StepExecutor(StepConfigLoader stepConfigLoader, RestTemplate restTemplate) {
        this.stepConfigLoader = stepConfigLoader;
        this.restTemplate = restTemplate;
    }

    /**
     * Executes a single test step: resolves config, validates required data fields,
     * builds and sends the HTTP request, then validates the response.
     *
     * @param step    the TestStep to execute
     * @param baseUrl the base URL for the API under test
     */
    public void execute(TestStep step, String baseUrl) {
        long startTime = System.currentTimeMillis();
        try {
            // Resolve the step configuration by matching the step description
            StepConfig config = stepConfigLoader.findByDescription(step.getDescription());

            // Validate that all required data fields are present
            validateRequiredFields(config, step);

            // Build the full URL with placeholder substitution
            String url = buildUrl(baseUrl, config.getEndpoint(), step.getData());

            // Build the request body from the JSON template
            String requestBody = buildRequestBody(config.getJsonTemplate(), step.getData());

            // Set up HTTP headers with JSON content type
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Execute the HTTP request
            HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, headers);
            HttpMethod method = HttpMethod.valueOf(config.getHttpMethod().toUpperCase());
            ResponseEntity<String> response = restTemplate.exchange(url, method, httpEntity, String.class);

            // Store response details on the step
            step.setResponseStatusCode(response.getStatusCodeValue());
            step.setResponseBody(response.getBody());

            // Validate response against verification rules
            validateResponse(config, step, response);

            // Mark step as passed if all verifications succeed
            step.setStatus("PASS");

        } catch (HttpStatusCodeException e) {
            // Capture HTTP error responses
            step.setResponseStatusCode(e.getRawStatusCode());
            step.setResponseBody(e.getResponseBodyAsString());
            step.setStatus("FAIL");
            step.setErrorMessage("HTTP " + e.getRawStatusCode() + ": " + e.getResponseBodyAsString());
        } catch (AssertionError e) {
            // Capture verification assertion failures
            step.setStatus("FAIL");
            step.setErrorMessage(e.getClass().getSimpleName() + ": " + e.getMessage());
        } catch (Exception e) {
            // Capture any other execution errors
            step.setStatus("FAIL");
            step.setErrorMessage(e.getClass().getSimpleName() + ": " + e.getMessage());
        } finally {
            // Record execution duration regardless of outcome
            step.setDurationMs(System.currentTimeMillis() - startTime);
        }
    }

    /**
     * Validates that the step's data map contains all required fields from the configuration.
     *
     * @param config the step configuration with required field definitions
     * @param step   the test step whose data is being validated
     * @throws MissingStepDataException if any required fields are missing
     */
    private void validateRequiredFields(StepConfig config, TestStep step) {
        List<String> requiredFields = config.getRequiredFields();
        if (requiredFields == null || requiredFields.isEmpty()) {
            return;
        }

        Map<String, String> data = step.getData();
        for (String field : requiredFields) {
            if (!data.containsKey(field) || data.get(field).isEmpty()) {
                throw new MissingStepDataException(
                        String.format("Required field '%s' is missing in Data column for step: %s",
                                field, step.getDescription()));
            }
        }
    }

    /**
     * Builds the full URL by replacing placeholders in the endpoint template with data values.
     * Placeholders use the format {fieldName}.
     *
     * @param baseUrl  the base API URL
     * @param endpoint the endpoint template with placeholders
     * @param data     the data map with replacement values
     * @return the fully resolved URL
     */
    private String buildUrl(String baseUrl, String endpoint, Map<String, String> data) {
        String resolvedEndpoint = endpoint;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            resolvedEndpoint = resolvedEndpoint.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return baseUrl + resolvedEndpoint;
    }

    /**
     * Builds the JSON request body by replacing placeholder tokens in the template with data values.
     * Placeholders use the format ${fieldName}.
     *
     * @param jsonTemplate the JSON template string with placeholders
     * @param data         the data map with replacement values
     * @return the resolved JSON body, or null if no template is defined
     */
    private String buildRequestBody(String jsonTemplate, Map<String, String> data) {
        if (jsonTemplate == null || jsonTemplate.isEmpty()) {
            return null;
        }
        String resolvedBody = jsonTemplate;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            resolvedBody = resolvedBody.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return resolvedBody;
    }

    /**
     * Validates the API response against the verification rules defined in the step configuration.
     *
     * @param config   the step configuration containing verification rules
     * @param step     the test step being verified
     * @param response the HTTP response from the API call
     */
    private void validateResponse(StepConfig config, TestStep step,
                                  ResponseEntity<String> response) {
        List<VerificationConfig> verifications = config.getVerifications();
        if (verifications == null || verifications.isEmpty()) {
            return;
        }

        for (VerificationConfig verification : verifications) {
            // Resolve ${...} placeholders in expectedValue using step data
            String resolvedExpectedValue = resolvePlaceholders(verification.getExpectedValue(), step.getData());

            switch (verification.getType()) {
                case "status-check":
                    // Verify HTTP status code matches expected value
                    int expectedStatus = Integer.parseInt(resolvedExpectedValue);
                    if (response.getStatusCodeValue() != expectedStatus) {
                        throw new AssertionError(String.format(
                                "Expected status %d but got %d", expectedStatus, response.getStatusCodeValue()));
                    }
                    break;

                case "json-path-equals":
                    // Verify a JSON path in the response body equals the resolved expected value
                    if (response.getBody() != null && verification.getJsonPath() != null) {
                        Object actualValue = JsonPath.read(response.getBody(), verification.getJsonPath());
                        if (!String.valueOf(actualValue).equals(resolvedExpectedValue)) {
                            throw new AssertionError(String.format(
                                    "JSON path '%s': expected '%s' but got '%s'",
                                    verification.getJsonPath(), resolvedExpectedValue, actualValue));
                        }
                    }
                    break;

                case "array-contains":
                    // Verify the response body (as JSON array) contains the resolved expected value
                    if (response.getBody() != null && verification.getJsonPath() != null) {
                        Object arrayValue = JsonPath.read(response.getBody(), verification.getJsonPath());
                        if (!String.valueOf(arrayValue).contains(resolvedExpectedValue)) {
                            throw new AssertionError(String.format(
                                    "Array at '%s' does not contain '%s'",
                                    verification.getJsonPath(), resolvedExpectedValue));
                        }
                    }
                    break;

                default:
                    // Unknown verification type, log a warning but do not fail
                    break;
            }
        }
    }

    /**
     * Resolves ${fieldName} placeholders in a string using step data values.
     * Used to substitute dynamic expected values in verification rules.
     *
     * @param template the string potentially containing ${...} placeholders
     * @param data     the data map with replacement values
     * @return the resolved string with placeholders replaced by actual data values
     */
    private String resolvePlaceholders(String template, Map<String, String> data) {
        if (template == null || data == null) {
            return template;
        }
        String resolved = template;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            resolved = resolved.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return resolved;
    }
}
