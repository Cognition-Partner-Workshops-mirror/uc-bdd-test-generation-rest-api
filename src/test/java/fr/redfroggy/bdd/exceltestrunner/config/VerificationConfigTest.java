package fr.redfroggy.bdd.exceltestrunner.config;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class VerificationConfigTest {

    @Test
    public void shouldCreateWithDefaultConstructor() {
        VerificationConfig config = new VerificationConfig();
        Assert.assertNull(config.getType());
        Assert.assertNull(config.getExpectedValue());
        Assert.assertNull(config.getJsonPath());
        Assert.assertNull(config.getParameters());
    }

    @Test
    public void shouldCreateWithTypeAndExpectedValue() {
        VerificationConfig config = new VerificationConfig("status-check", "200");
        Assert.assertEquals("status-check", config.getType());
        Assert.assertEquals("200", config.getExpectedValue());
    }

    @Test
    public void shouldSetAndGetAllFields() {
        VerificationConfig config = new VerificationConfig();
        config.setType("json-path-equals");
        config.setExpectedValue("testUser");
        config.setJsonPath("$.username");
        Map<String, String> params = new HashMap<>();
        params.put("key", "value");
        config.setParameters(params);

        Assert.assertEquals("json-path-equals", config.getType());
        Assert.assertEquals("testUser", config.getExpectedValue());
        Assert.assertEquals("$.username", config.getJsonPath());
        Assert.assertEquals(1, config.getParameters().size());
        Assert.assertEquals("value", config.getParameters().get("key"));
    }
}
