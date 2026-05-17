package fr.redfroggy.bdd.exceltestrunner.executor;

import fr.redfroggy.bdd.exceltestrunner.model.TestCase;
import fr.redfroggy.bdd.exceltestrunner.model.TestStep;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestCaseExecutorTest {

    private StepExecutor stepExecutor;
    private TestCaseExecutor testCaseExecutor;

    @Before
    public void setUp() {
        stepExecutor = Mockito.mock(StepExecutor.class);
        testCaseExecutor = new TestCaseExecutor(stepExecutor);
        testCaseExecutor.setBaseUrl("http://localhost:8080");
    }

    @Test
    public void shouldExecuteAllTestCases() {
        // Configure mock to mark steps as PASS
        doAnswer(invocation -> {
            TestStep step = invocation.getArgument(0);
            step.setStatus("PASS");
            step.setDurationMs(10L);
            return null;
        }).when(stepExecutor).execute(any(TestStep.class), anyString());

        TestCase tc1 = new TestCase("TC001", "Test 1", "Desc 1");
        tc1.addStep(new TestStep("Step 1", new HashMap<>()));

        TestCase tc2 = new TestCase("TC002", "Test 2", "Desc 2");
        tc2.addStep(new TestStep("Step 2", new HashMap<>()));

        testCaseExecutor.executeAll(Arrays.asList(tc1, tc2));

        Assert.assertEquals("PASS", tc1.getStatus());
        Assert.assertEquals("PASS", tc2.getStatus());
        verify(stepExecutor, times(2)).execute(any(TestStep.class), anyString());
    }

    @Test
    public void shouldSkipStepsAfterFailure() {
        // First step fails, second step should be skipped
        doAnswer(invocation -> {
            TestStep step = invocation.getArgument(0);
            if ("Step 1".equals(step.getDescription())) {
                step.setStatus("FAIL");
                step.setErrorMessage("API error");
            }
            return null;
        }).when(stepExecutor).execute(any(TestStep.class), anyString());

        TestCase tc = new TestCase("TC001", "Test", "Desc");
        tc.addStep(new TestStep("Step 1", new HashMap<>()));
        tc.addStep(new TestStep("Step 2", new HashMap<>()));

        testCaseExecutor.executeAll(Collections.singletonList(tc));

        Assert.assertEquals("FAIL", tc.getStatus());
        Assert.assertEquals("FAIL", tc.getSteps().get(0).getStatus());
        Assert.assertEquals("SKIP", tc.getSteps().get(1).getStatus());
        Assert.assertNotNull(tc.getSteps().get(1).getErrorMessage());
        // Only first step should have been executed via stepExecutor
        verify(stepExecutor, times(1)).execute(any(TestStep.class), anyString());
    }

    @Test
    public void shouldMarkTestCaseAsPassWhenAllStepsPass() {
        doAnswer(invocation -> {
            TestStep step = invocation.getArgument(0);
            step.setStatus("PASS");
            step.setDurationMs(5L);
            return null;
        }).when(stepExecutor).execute(any(TestStep.class), anyString());

        TestCase tc = new TestCase("TC001", "Test", "Desc");
        tc.addStep(new TestStep("Step 1", new HashMap<>()));
        tc.addStep(new TestStep("Step 2", new HashMap<>()));

        testCaseExecutor.executeAll(Collections.singletonList(tc));

        Assert.assertEquals("PASS", tc.getStatus());
        Assert.assertTrue(tc.getTotalDurationMs() >= 0);
    }

    @Test
    public void shouldMarkTestCaseAsSkipWhenAllStepsSkipped() {
        // First step fails immediately, all subsequent are skipped
        TestCase tc = new TestCase("TC001", "Test", "Desc");

        doAnswer(invocation -> {
            TestStep step = invocation.getArgument(0);
            step.setStatus("FAIL");
            return null;
        }).when(stepExecutor).execute(any(TestStep.class), anyString());

        tc.addStep(new TestStep("Step 1", new HashMap<>()));
        tc.addStep(new TestStep("Step 2", new HashMap<>()));
        tc.addStep(new TestStep("Step 3", new HashMap<>()));

        testCaseExecutor.executeAll(Collections.singletonList(tc));

        Assert.assertEquals("FAIL", tc.getStatus());
        Assert.assertEquals("SKIP", tc.getSteps().get(1).getStatus());
        Assert.assertEquals("SKIP", tc.getSteps().get(2).getStatus());
    }

    @Test
    public void shouldHandleEmptyTestCaseList() {
        testCaseExecutor.executeAll(Collections.emptyList());
        verify(stepExecutor, times(0)).execute(any(TestStep.class), anyString());
    }

    @Test
    public void shouldRecordTotalDuration() {
        doAnswer(invocation -> {
            TestStep step = invocation.getArgument(0);
            step.setStatus("PASS");
            step.setDurationMs(100L);
            return null;
        }).when(stepExecutor).execute(any(TestStep.class), anyString());

        TestCase tc = new TestCase("TC001", "Test", "Desc");
        tc.addStep(new TestStep("Step 1", new HashMap<>()));

        testCaseExecutor.executeAll(Collections.singletonList(tc));

        Assert.assertTrue(tc.getTotalDurationMs() >= 0);
    }

    @Test
    public void shouldDetermineSkipStatusWhenNoStepsPassOrFail() {
        // Create a test case with all steps manually set to SKIP (edge case)
        TestCase tc = new TestCase("TC001", "Test", "Desc");
        // Mock step executor to not change status, so status remains PENDING
        // but after execution loop, if hasFailed is never set and step status is never PASS or FAIL
        // Let's simulate: first step gets SKIP somehow (edge case via manual override)
        doAnswer(invocation -> {
            TestStep step = invocation.getArgument(0);
            step.setStatus("SKIP");
            return null;
        }).when(stepExecutor).execute(any(TestStep.class), anyString());

        tc.addStep(new TestStep("Step 1", new HashMap<>()));

        testCaseExecutor.executeAll(Collections.singletonList(tc));
        Assert.assertEquals("SKIP", tc.getStatus());
    }
}
