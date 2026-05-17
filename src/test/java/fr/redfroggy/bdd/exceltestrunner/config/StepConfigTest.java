package fr.redfroggy.bdd.exceltestrunner.config;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StepConfigTest {

    @Test
    public void shouldCreateWithDefaultConstructor() {
        StepConfig config = new StepConfig();
        Assert.assertNull(config.getStepPattern());
        Assert.assertNull(config.getHttpMethod());
        Assert.assertNull(config.getEndpoint());
        Assert.assertNull(config.getJsonTemplate());
        Assert.assertNull(config.getRequiredFields());
        Assert.assertNull(config.getVerifications());
    }

    @Test
    public void shouldSetAndGetAllFields() {
        StepConfig config = new StepConfig();
        config.setStepPattern("Create a new user");
        config.setHttpMethod("POST");
        config.setEndpoint("/api/users");
        config.setJsonTemplate("{\"name\": \"${name}\"}");
        config.setRequiredFields(Arrays.asList("name", "email"));

        VerificationConfig verification = new VerificationConfig("status-check", "201");
        config.setVerifications(Collections.singletonList(verification));

        Assert.assertEquals("Create a new user", config.getStepPattern());
        Assert.assertEquals("POST", config.getHttpMethod());
        Assert.assertEquals("/api/users", config.getEndpoint());
        Assert.assertEquals("{\"name\": \"${name}\"}", config.getJsonTemplate());
        Assert.assertEquals(2, config.getRequiredFields().size());
        Assert.assertEquals("name", config.getRequiredFields().get(0));
        Assert.assertEquals("email", config.getRequiredFields().get(1));
        Assert.assertEquals(1, config.getVerifications().size());
        Assert.assertEquals("status-check", config.getVerifications().get(0).getType());
    }

    @Test
    public void shouldAcceptNullVerifications() {
        StepConfig config = new StepConfig();
        config.setVerifications(null);
        Assert.assertNull(config.getVerifications());
    }

    @Test
    public void shouldAcceptEmptyRequiredFields() {
        StepConfig config = new StepConfig();
        List<String> emptyList = Collections.emptyList();
        config.setRequiredFields(emptyList);
        Assert.assertTrue(config.getRequiredFields().isEmpty());
    }
}
