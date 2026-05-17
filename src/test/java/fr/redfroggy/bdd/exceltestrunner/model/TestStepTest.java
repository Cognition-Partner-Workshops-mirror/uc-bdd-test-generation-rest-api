package fr.redfroggy.bdd.exceltestrunner.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestStepTest {

    @Test
    public void shouldCreateWithDefaultConstructor() {
        TestStep step = new TestStep();
        Assert.assertNotNull(step.getData());
        Assert.assertTrue(step.getData().isEmpty());
        Assert.assertEquals("PENDING", step.getStatus());
        Assert.assertNull(step.getDescription());
        Assert.assertNull(step.getErrorMessage());
        Assert.assertNull(step.getResponseBody());
        Assert.assertEquals(0, step.getDurationMs());
        Assert.assertEquals(0, step.getResponseStatusCode());
    }

    @Test
    public void shouldCreateWithDescriptionAndData() {
        Map<String, String> data = new HashMap<>();
        data.put("username", "john");
        TestStep step = new TestStep("Create user", data);
        Assert.assertEquals("Create user", step.getDescription());
        Assert.assertEquals("john", step.getData().get("username"));
        Assert.assertEquals("PENDING", step.getStatus());
    }

    @Test
    public void shouldCreateWithNullData() {
        TestStep step = new TestStep("Some step", null);
        Assert.assertNotNull(step.getData());
        Assert.assertTrue(step.getData().isEmpty());
    }

    @Test
    public void shouldSetAndGetAllFields() {
        TestStep step = new TestStep();
        step.setDescription("Test step");
        Map<String, String> data = new HashMap<>();
        data.put("key", "value");
        step.setData(data);
        step.setStatus("PASS");
        step.setErrorMessage("some error");
        step.setDurationMs(150L);
        step.setResponseStatusCode(200);
        step.setResponseBody("{\"ok\":true}");

        Assert.assertEquals("Test step", step.getDescription());
        Assert.assertEquals("value", step.getData().get("key"));
        Assert.assertEquals("PASS", step.getStatus());
        Assert.assertEquals("some error", step.getErrorMessage());
        Assert.assertEquals(150L, step.getDurationMs());
        Assert.assertEquals(200, step.getResponseStatusCode());
        Assert.assertEquals("{\"ok\":true}", step.getResponseBody());
    }
}
