package fr.redfroggy.bdd.exceltestrunner.executor;

import fr.redfroggy.bdd.exceltestrunner.config.StepConfig;
import fr.redfroggy.bdd.exceltestrunner.config.StepConfigLoader;
import fr.redfroggy.bdd.exceltestrunner.config.VerificationConfig;
import fr.redfroggy.bdd.exceltestrunner.exception.MissingStepDataException;
import fr.redfroggy.bdd.exceltestrunner.exception.StepConfigNotFoundException;
import fr.redfroggy.bdd.exceltestrunner.model.TestStep;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class StepExecutorTest {

    private StepConfigLoader stepConfigLoader;
    private RestTemplate restTemplate;
    private StepExecutor stepExecutor;

    @Before
    public void setUp() {
        stepConfigLoader = Mockito.mock(StepConfigLoader.class);
        restTemplate = Mockito.mock(RestTemplate.class);
        stepExecutor = new StepExecutor(stepConfigLoader, restTemplate);
    }

    @Test
    public void shouldExecuteStepSuccessfully() {
        StepConfig config = createStepConfig("POST", "/api/users", "{\"name\":\"${name}\"}",
                Collections.singletonList("name"), null);
        when(stepConfigLoader.findByDescription("Create user")).thenReturn(config);

        ResponseEntity<String> response = new ResponseEntity<>("{\"id\":1}", HttpStatus.CREATED);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenReturn(response);

        Map<String, String> data = new HashMap<>();
        data.put("name", "John");
        TestStep step = new TestStep("Create user", data);

        stepExecutor.execute(step, "http://localhost:8080");

        Assert.assertEquals("PASS", step.getStatus());
        Assert.assertEquals(201, step.getResponseStatusCode());
        Assert.assertEquals("{\"id\":1}", step.getResponseBody());
    }

    @Test
    public void shouldHandleHttpErrorResponse() {
        StepConfig config = createStepConfig("GET", "/api/users/{userId}", null,
                Collections.singletonList("userId"), null);
        when(stepConfigLoader.findByDescription("Get user")).thenReturn(config);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

        Map<String, String> data = new HashMap<>();
        data.put("userId", "999");
        TestStep step = new TestStep("Get user", data);

        stepExecutor.execute(step, "http://localhost:8080");

        Assert.assertEquals("FAIL", step.getStatus());
        Assert.assertEquals(404, step.getResponseStatusCode());
        Assert.assertNotNull(step.getErrorMessage());
    }

    @Test
    public void shouldFailWhenConfigNotFound() {
        when(stepConfigLoader.findByDescription(anyString()))
                .thenThrow(new StepConfigNotFoundException("No config found"));

        TestStep step = new TestStep("Unknown step", new HashMap<>());
        stepExecutor.execute(step, "http://localhost:8080");

        Assert.assertEquals("FAIL", step.getStatus());
        Assert.assertTrue(step.getErrorMessage().contains("StepConfigNotFoundException"));
    }

    @Test
    public void shouldFailWhenRequiredFieldMissing() {
        StepConfig config = createStepConfig("POST", "/api/users", "{\"name\":\"${name}\"}",
                Arrays.asList("name", "email"), null);
        when(stepConfigLoader.findByDescription("Create user")).thenReturn(config);

        Map<String, String> data = new HashMap<>();
        data.put("name", "John");
        // "email" is missing
        TestStep step = new TestStep("Create user", data);

        stepExecutor.execute(step, "http://localhost:8080");

        Assert.assertEquals("FAIL", step.getStatus());
        Assert.assertTrue(step.getErrorMessage().contains("MissingStepDataException"));
    }

    @Test
    public void shouldExecuteStepWithNoRequiredFields() {
        StepConfig config = createStepConfig("GET", "/api/users", null, null, null);
        when(stepConfigLoader.findByDescription("List users")).thenReturn(config);

        ResponseEntity<String> response = new ResponseEntity<>("[]", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenReturn(response);

        TestStep step = new TestStep("List users", new HashMap<>());
        stepExecutor.execute(step, "http://localhost:8080");

        Assert.assertEquals("PASS", step.getStatus());
    }

    @Test
    public void shouldExecuteStepWithEmptyRequiredFields() {
        StepConfig config = createStepConfig("GET", "/api/users", null,
                Collections.emptyList(), null);
        when(stepConfigLoader.findByDescription("List users")).thenReturn(config);

        ResponseEntity<String> response = new ResponseEntity<>("[]", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenReturn(response);

        TestStep step = new TestStep("List users", new HashMap<>());
        stepExecutor.execute(step, "http://localhost:8080");

        Assert.assertEquals("PASS", step.getStatus());
    }

    @Test
    public void shouldFailWhenRequiredFieldIsEmpty() {
        StepConfig config = createStepConfig("POST", "/api/users", "{\"name\":\"${name}\"}",
                Collections.singletonList("name"), null);
        when(stepConfigLoader.findByDescription("Create user")).thenReturn(config);

        Map<String, String> data = new HashMap<>();
        data.put("name", "");
        TestStep step = new TestStep("Create user", data);

        stepExecutor.execute(step, "http://localhost:8080");

        Assert.assertEquals("FAIL", step.getStatus());
        Assert.assertTrue(step.getErrorMessage().contains("MissingStepDataException"));
    }

    @Test
    public void shouldExecuteWithNullJsonTemplate() {
        StepConfig config = createStepConfig("GET", "/api/users", null, null, null);
        when(stepConfigLoader.findByDescription("List users")).thenReturn(config);

        ResponseEntity<String> response = new ResponseEntity<>("[]", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenReturn(response);

        TestStep step = new TestStep("List users", new HashMap<>());
        stepExecutor.execute(step, "http://localhost:8080");

        Assert.assertEquals("PASS", step.getStatus());
    }

    @Test
    public void shouldExecuteWithEmptyJsonTemplate() {
        StepConfig config = createStepConfig("GET", "/api/users", "", null, null);
        when(stepConfigLoader.findByDescription("List users")).thenReturn(config);

        ResponseEntity<String> response = new ResponseEntity<>("[]", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenReturn(response);

        TestStep step = new TestStep("List users", new HashMap<>());
        stepExecutor.execute(step, "http://localhost:8080");

        Assert.assertEquals("PASS", step.getStatus());
    }

    @Test
    public void shouldVerifyStatusCheck() {
        VerificationConfig verification = new VerificationConfig("status-check", "201");
        StepConfig config = createStepConfig("POST", "/api/users", "{\"name\":\"${name}\"}",
                Collections.singletonList("name"), Collections.singletonList(verification));
        when(stepConfigLoader.findByDescription("Create user")).thenReturn(config);

        ResponseEntity<String> response = new ResponseEntity<>("{\"id\":1}", HttpStatus.CREATED);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenReturn(response);

        Map<String, String> data = new HashMap<>();
        data.put("name", "John");
        TestStep step = new TestStep("Create user", data);

        stepExecutor.execute(step, "http://localhost:8080");
        Assert.assertEquals("PASS", step.getStatus());
    }

    @Test
    public void shouldFailStatusCheckVerification() {
        VerificationConfig verification = new VerificationConfig("status-check", "201");
        StepConfig config = createStepConfig("POST", "/api/users", "{\"name\":\"${name}\"}",
                Collections.singletonList("name"), Collections.singletonList(verification));
        when(stepConfigLoader.findByDescription("Create user")).thenReturn(config);

        ResponseEntity<String> response = new ResponseEntity<>("{\"error\":\"bad\"}", HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenReturn(response);

        Map<String, String> data = new HashMap<>();
        data.put("name", "John");
        TestStep step = new TestStep("Create user", data);

        stepExecutor.execute(step, "http://localhost:8080");
        Assert.assertEquals("FAIL", step.getStatus());
    }

    @Test
    public void shouldVerifyJsonPathEquals() {
        VerificationConfig verification = new VerificationConfig();
        verification.setType("json-path-equals");
        verification.setJsonPath("$.name");
        verification.setExpectedValue("John");
        StepConfig config = createStepConfig("GET", "/api/users/1", null,
                null, Collections.singletonList(verification));
        when(stepConfigLoader.findByDescription("Get user")).thenReturn(config);

        ResponseEntity<String> response = new ResponseEntity<>("{\"name\":\"John\"}", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenReturn(response);

        TestStep step = new TestStep("Get user", new HashMap<>());
        stepExecutor.execute(step, "http://localhost:8080");
        Assert.assertEquals("PASS", step.getStatus());
    }

    @Test
    public void shouldFailJsonPathEqualsVerification() {
        VerificationConfig verification = new VerificationConfig();
        verification.setType("json-path-equals");
        verification.setJsonPath("$.name");
        verification.setExpectedValue("John");
        StepConfig config = createStepConfig("GET", "/api/users/1", null,
                null, Collections.singletonList(verification));
        when(stepConfigLoader.findByDescription("Get user")).thenReturn(config);

        ResponseEntity<String> response = new ResponseEntity<>("{\"name\":\"Jane\"}", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenReturn(response);

        TestStep step = new TestStep("Get user", new HashMap<>());
        stepExecutor.execute(step, "http://localhost:8080");
        Assert.assertEquals("FAIL", step.getStatus());
    }

    @Test
    public void shouldVerifyArrayContains() {
        VerificationConfig verification = new VerificationConfig();
        verification.setType("array-contains");
        verification.setJsonPath("$.names");
        verification.setExpectedValue("John");
        StepConfig config = createStepConfig("GET", "/api/users", null,
                null, Collections.singletonList(verification));
        when(stepConfigLoader.findByDescription("List users")).thenReturn(config);

        ResponseEntity<String> response = new ResponseEntity<>("{\"names\":[\"John\",\"Jane\"]}", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenReturn(response);

        TestStep step = new TestStep("List users", new HashMap<>());
        stepExecutor.execute(step, "http://localhost:8080");
        Assert.assertEquals("PASS", step.getStatus());
    }

    @Test
    public void shouldFailArrayContainsVerification() {
        VerificationConfig verification = new VerificationConfig();
        verification.setType("array-contains");
        verification.setJsonPath("$.names");
        verification.setExpectedValue("Bob");
        StepConfig config = createStepConfig("GET", "/api/users", null,
                null, Collections.singletonList(verification));
        when(stepConfigLoader.findByDescription("List users")).thenReturn(config);

        ResponseEntity<String> response = new ResponseEntity<>("{\"names\":[\"John\",\"Jane\"]}", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenReturn(response);

        TestStep step = new TestStep("List users", new HashMap<>());
        stepExecutor.execute(step, "http://localhost:8080");
        Assert.assertEquals("FAIL", step.getStatus());
    }

    @Test
    public void shouldHandleUnknownVerificationType() {
        VerificationConfig verification = new VerificationConfig("unknown-type", "value");
        StepConfig config = createStepConfig("GET", "/api/users", null,
                null, Collections.singletonList(verification));
        when(stepConfigLoader.findByDescription("List users")).thenReturn(config);

        ResponseEntity<String> response = new ResponseEntity<>("[]", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenReturn(response);

        TestStep step = new TestStep("List users", new HashMap<>());
        stepExecutor.execute(step, "http://localhost:8080");
        Assert.assertEquals("PASS", step.getStatus());
    }

    @Test
    public void shouldHandleNullVerifications() {
        StepConfig config = createStepConfig("GET", "/api/users", null, null, null);
        when(stepConfigLoader.findByDescription("List users")).thenReturn(config);

        ResponseEntity<String> response = new ResponseEntity<>("[]", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenReturn(response);

        TestStep step = new TestStep("List users", new HashMap<>());
        stepExecutor.execute(step, "http://localhost:8080");
        Assert.assertEquals("PASS", step.getStatus());
    }

    @Test
    public void shouldHandleEmptyVerifications() {
        StepConfig config = createStepConfig("GET", "/api/users", null,
                null, Collections.emptyList());
        when(stepConfigLoader.findByDescription("List users")).thenReturn(config);

        ResponseEntity<String> response = new ResponseEntity<>("[]", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenReturn(response);

        TestStep step = new TestStep("List users", new HashMap<>());
        stepExecutor.execute(step, "http://localhost:8080");
        Assert.assertEquals("PASS", step.getStatus());
    }

    @Test
    public void shouldHandleJsonPathWithNullBody() {
        VerificationConfig verification = new VerificationConfig();
        verification.setType("json-path-equals");
        verification.setJsonPath("$.name");
        verification.setExpectedValue("John");
        StepConfig config = createStepConfig("GET", "/api/users/1", null,
                null, Collections.singletonList(verification));
        when(stepConfigLoader.findByDescription("Get user")).thenReturn(config);

        ResponseEntity<String> response = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenReturn(response);

        TestStep step = new TestStep("Get user", new HashMap<>());
        stepExecutor.execute(step, "http://localhost:8080");
        Assert.assertEquals("PASS", step.getStatus());
    }

    @Test
    public void shouldHandleArrayContainsWithNullBody() {
        VerificationConfig verification = new VerificationConfig();
        verification.setType("array-contains");
        verification.setJsonPath("$.names");
        verification.setExpectedValue("John");
        StepConfig config = createStepConfig("GET", "/api/users", null,
                null, Collections.singletonList(verification));
        when(stepConfigLoader.findByDescription("List users")).thenReturn(config);

        ResponseEntity<String> response = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenReturn(response);

        TestStep step = new TestStep("List users", new HashMap<>());
        stepExecutor.execute(step, "http://localhost:8080");
        Assert.assertEquals("PASS", step.getStatus());
    }

    @Test
    public void shouldHandleJsonPathWithNullJsonPath() {
        VerificationConfig verification = new VerificationConfig();
        verification.setType("json-path-equals");
        verification.setJsonPath(null);
        verification.setExpectedValue("John");
        StepConfig config = createStepConfig("GET", "/api/users/1", null,
                null, Collections.singletonList(verification));
        when(stepConfigLoader.findByDescription("Get user")).thenReturn(config);

        ResponseEntity<String> response = new ResponseEntity<>("{\"name\":\"John\"}", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenReturn(response);

        TestStep step = new TestStep("Get user", new HashMap<>());
        stepExecutor.execute(step, "http://localhost:8080");
        Assert.assertEquals("PASS", step.getStatus());
    }

    @Test
    public void shouldHandleArrayContainsWithNullJsonPath() {
        VerificationConfig verification = new VerificationConfig();
        verification.setType("array-contains");
        verification.setJsonPath(null);
        verification.setExpectedValue("John");
        StepConfig config = createStepConfig("GET", "/api/users", null,
                null, Collections.singletonList(verification));
        when(stepConfigLoader.findByDescription("List users")).thenReturn(config);

        ResponseEntity<String> response = new ResponseEntity<>("{\"names\":[\"John\"]}", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenReturn(response);

        TestStep step = new TestStep("List users", new HashMap<>());
        stepExecutor.execute(step, "http://localhost:8080");
        Assert.assertEquals("PASS", step.getStatus());
    }

    @Test
    public void shouldRecordDuration() {
        StepConfig config = createStepConfig("GET", "/api/users", null, null, null);
        when(stepConfigLoader.findByDescription("List users")).thenReturn(config);

        ResponseEntity<String> response = new ResponseEntity<>("[]", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenReturn(response);

        TestStep step = new TestStep("List users", new HashMap<>());
        stepExecutor.execute(step, "http://localhost:8080");

        Assert.assertTrue(step.getDurationMs() >= 0);
    }

    @Test
    public void shouldHandleGenericException() {
        StepConfig config = createStepConfig("GET", "/api/users", null, null, null);
        when(stepConfigLoader.findByDescription("List users")).thenReturn(config);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        TestStep step = new TestStep("List users", new HashMap<>());
        stepExecutor.execute(step, "http://localhost:8080");

        Assert.assertEquals("FAIL", step.getStatus());
        Assert.assertTrue(step.getErrorMessage().contains("RuntimeException"));
        Assert.assertTrue(step.getErrorMessage().contains("Connection refused"));
    }

    private StepConfig createStepConfig(String method, String endpoint, String jsonTemplate,
                                         java.util.List<String> requiredFields,
                                         java.util.List<VerificationConfig> verifications) {
        StepConfig config = new StepConfig();
        config.setStepPattern(".*");
        config.setHttpMethod(method);
        config.setEndpoint(endpoint);
        config.setJsonTemplate(jsonTemplate);
        config.setRequiredFields(requiredFields);
        config.setVerifications(verifications);
        return config;
    }
}
