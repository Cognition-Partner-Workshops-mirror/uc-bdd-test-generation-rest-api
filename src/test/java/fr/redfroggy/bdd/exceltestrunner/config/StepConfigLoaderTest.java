package fr.redfroggy.bdd.exceltestrunner.config;

import fr.redfroggy.bdd.exceltestrunner.exception.StepConfigNotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StepConfigLoaderTest {

    private StepConfigLoader loader;

    @Before
    public void setUp() throws IOException {
        loader = new StepConfigLoader();
        loader.loadConfig();
    }

    @Test
    public void shouldLoadConfigFromYaml() {
        Assert.assertNotNull(loader.getStepConfigs());
        Assert.assertFalse(loader.getStepConfigs().isEmpty());
    }

    @Test
    public void shouldFindByDescription() {
        StepConfig config = loader.findByDescription("Create a new user");
        Assert.assertNotNull(config);
        Assert.assertEquals("POST", config.getHttpMethod());
        Assert.assertEquals("/api/users", config.getEndpoint());
    }

    @Test
    public void shouldFindByDescriptionCaseInsensitive() {
        StepConfig config = loader.findByDescription("create a new user");
        Assert.assertNotNull(config);
        Assert.assertEquals("POST", config.getHttpMethod());
    }

    @Test(expected = StepConfigNotFoundException.class)
    public void shouldThrowExceptionWhenNoMatchFound() {
        loader.findByDescription("Non-existent step description that won't match anything");
    }

    @Test
    public void shouldSetStepConfigs() {
        List<StepConfig> customConfigs = new ArrayList<>();
        StepConfig config = new StepConfig();
        config.setStepPattern("Custom step");
        config.setHttpMethod("GET");
        config.setEndpoint("/api/custom");
        customConfigs.add(config);

        loader.setStepConfigs(customConfigs);
        Assert.assertEquals(1, loader.getStepConfigs().size());
        Assert.assertEquals("Custom step", loader.getStepConfigs().get(0).getStepPattern());
    }

    @Test
    public void shouldLoadStepWithVerifications() {
        StepConfig config = loader.findByDescription("Create a new user");
        Assert.assertNotNull(config.getVerifications());
        Assert.assertFalse(config.getVerifications().isEmpty());
        Assert.assertEquals("status-check", config.getVerifications().get(0).getType());
        Assert.assertEquals("201", config.getVerifications().get(0).getExpectedValue());
    }

    @Test
    public void shouldLoadStepWithRequiredFields() {
        StepConfig config = loader.findByDescription("Create a new user");
        Assert.assertNotNull(config.getRequiredFields());
        Assert.assertTrue(config.getRequiredFields().contains("username"));
        Assert.assertTrue(config.getRequiredFields().contains("email"));
        Assert.assertTrue(config.getRequiredFields().contains("password"));
    }

    @Test
    public void shouldFindGetUserByIdStep() {
        StepConfig config = loader.findByDescription("Get user by ID");
        Assert.assertNotNull(config);
        Assert.assertEquals("GET", config.getHttpMethod());
        Assert.assertEquals("/api/users/{userId}", config.getEndpoint());
    }

    @Test
    public void shouldFindDeleteUserStep() {
        StepConfig config = loader.findByDescription("Delete user");
        Assert.assertNotNull(config);
        Assert.assertEquals("DELETE", config.getHttpMethod());
    }

    @Test
    public void shouldFindListAllUsersStep() {
        StepConfig config = loader.findByDescription("List all users");
        Assert.assertNotNull(config);
        Assert.assertEquals("GET", config.getHttpMethod());
        Assert.assertEquals("/api/users", config.getEndpoint());
    }

    @Test
    public void shouldFindUpdateUserDetailsStep() {
        StepConfig config = loader.findByDescription("Update user details");
        Assert.assertNotNull(config);
        Assert.assertEquals("PUT", config.getHttpMethod());
    }

    @Test
    public void shouldFindVerifyUserExistsStep() {
        StepConfig config = loader.findByDescription("Verify user exists");
        Assert.assertNotNull(config);
        Assert.assertEquals("GET", config.getHttpMethod());
        // Should have multiple verifications including json-path-equals
        Assert.assertTrue(config.getVerifications().size() >= 2);
    }
}
