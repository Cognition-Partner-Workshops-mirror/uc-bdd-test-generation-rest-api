package fr.redfroggy.bdd.exceltestrunner.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestCaseTest {

    @Test
    public void shouldCreateWithDefaultConstructor() {
        TestCase tc = new TestCase();
        Assert.assertNotNull(tc.getSteps());
        Assert.assertTrue(tc.getSteps().isEmpty());
        Assert.assertEquals("PENDING", tc.getStatus());
        Assert.assertNull(tc.getTcNumber());
        Assert.assertNull(tc.getTitle());
        Assert.assertNull(tc.getDescription());
        Assert.assertNull(tc.getExecutionStatus());
        Assert.assertNull(tc.getExecutionDate());
        Assert.assertEquals(0L, tc.getTotalDurationMs());
    }

    @Test
    public void shouldCreateWithArguments() {
        TestCase tc = new TestCase("TC001", "User Creation", "Create a new user");
        Assert.assertEquals("TC001", tc.getTcNumber());
        Assert.assertEquals("User Creation", tc.getTitle());
        Assert.assertEquals("Create a new user", tc.getDescription());
        Assert.assertNotNull(tc.getSteps());
        Assert.assertTrue(tc.getSteps().isEmpty());
        Assert.assertEquals("PENDING", tc.getStatus());
    }

    @Test
    public void shouldAddStep() {
        TestCase tc = new TestCase("TC001", "Test", "Desc");
        TestStep step = new TestStep("Step 1", null);
        tc.addStep(step);
        Assert.assertEquals(1, tc.getSteps().size());
        Assert.assertEquals("Step 1", tc.getSteps().get(0).getDescription());
    }

    @Test
    public void shouldSetAndGetAllFields() {
        TestCase tc = new TestCase();
        tc.setTcNumber("TC002");
        tc.setTitle("Title 2");
        tc.setDescription("Description 2");
        tc.setStatus("PASS");
        tc.setTotalDurationMs(500L);
        // Set Execution status and Execution Date fields from Excel template
        tc.setExecutionStatus("Completed");
        tc.setExecutionDate("2026-01-15");

        List<TestStep> steps = new ArrayList<>();
        steps.add(new TestStep("S1", null));
        tc.setSteps(steps);

        Assert.assertEquals("TC002", tc.getTcNumber());
        Assert.assertEquals("Title 2", tc.getTitle());
        Assert.assertEquals("Description 2", tc.getDescription());
        Assert.assertEquals("PASS", tc.getStatus());
        Assert.assertEquals(500L, tc.getTotalDurationMs());
        Assert.assertEquals(1, tc.getSteps().size());
        // Verify Execution status and Execution Date fields
        Assert.assertEquals("Completed", tc.getExecutionStatus());
        Assert.assertEquals("2026-01-15", tc.getExecutionDate());
    }
}
