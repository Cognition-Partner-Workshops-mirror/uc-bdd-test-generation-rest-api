package fr.redfroggy.bdd.exceltestrunner.report;

import fr.redfroggy.bdd.exceltestrunner.model.TestCase;
import fr.redfroggy.bdd.exceltestrunner.model.TestStep;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class ReportGeneratorTest {

    private ReportGenerator reportGenerator;

    @Before
    public void setUp() {
        reportGenerator = new ReportGenerator();
    }

    @Test
    public void shouldGenerateHtmlReport() {
        TestStep step = new TestStep("Create user", new HashMap<>());
        step.setStatus("PASS");
        step.setDurationMs(50L);

        TestCase tc = new TestCase("TC001", "User Test", "Test user creation");
        tc.addStep(step);
        tc.setStatus("PASS");
        tc.setTotalDurationMs(50L);
        // Set Execution status and Execution Date for report column rendering
        tc.setExecutionStatus("Pending");
        tc.setExecutionDate("2026-01-15");

        ExecutionReport report = new ExecutionReport(Collections.singletonList(tc));

        String html = reportGenerator.generateHtml(report);

        Assert.assertNotNull(html);
        Assert.assertTrue(html.contains("<!DOCTYPE html>"));
        Assert.assertTrue(html.contains("Excel Test Runner - Execution Report"));
        Assert.assertTrue(html.contains("TC001"));
        Assert.assertTrue(html.contains("User Test"));
        Assert.assertTrue(html.contains("Create user"));
        Assert.assertTrue(html.contains("PASS"));
        // Verify Execution Status and Execution Date columns appear in the report
        Assert.assertTrue(html.contains("Execution Status"));
        Assert.assertTrue(html.contains("Execution Date"));
        Assert.assertTrue(html.contains("Pending"));
        Assert.assertTrue(html.contains("2026-01-15"));
    }

    @Test
    public void shouldIncludeFailedStepDetails() {
        TestStep step = new TestStep("Delete user", new HashMap<>());
        step.setStatus("FAIL");
        step.setErrorMessage("HTTP 404: Not Found");
        step.setDurationMs(30L);

        TestCase tc = new TestCase("TC002", "Delete Test", "Test user deletion");
        tc.addStep(step);
        tc.setStatus("FAIL");
        tc.setTotalDurationMs(30L);

        ExecutionReport report = new ExecutionReport(Collections.singletonList(tc));

        String html = reportGenerator.generateHtml(report);

        Assert.assertTrue(html.contains("FAIL"));
        Assert.assertTrue(html.contains("HTTP 404: Not Found"));
        Assert.assertTrue(html.contains("error-msg"));
    }

    @Test
    public void shouldIncludeSkippedSteps() {
        TestStep step1 = new TestStep("Step 1", new HashMap<>());
        step1.setStatus("FAIL");
        step1.setDurationMs(10L);
        step1.setErrorMessage("Error");

        TestStep step2 = new TestStep("Step 2", new HashMap<>());
        step2.setStatus("SKIP");
        step2.setDurationMs(0L);

        TestCase tc = new TestCase("TC003", "Skip Test", "Test skipping");
        tc.addStep(step1);
        tc.addStep(step2);
        tc.setStatus("FAIL");
        tc.setTotalDurationMs(10L);

        ExecutionReport report = new ExecutionReport(Collections.singletonList(tc));

        String html = reportGenerator.generateHtml(report);

        Assert.assertTrue(html.contains("SKIP"));
        Assert.assertTrue(html.contains("skip"));
    }

    @Test
    public void shouldIncludeSummarySection() {
        TestCase tc1 = new TestCase("TC001", "Test 1", "");
        tc1.setStatus("PASS");
        tc1.setTotalDurationMs(100L);

        TestCase tc2 = new TestCase("TC002", "Test 2", "");
        tc2.setStatus("FAIL");
        tc2.setTotalDurationMs(200L);

        ExecutionReport report = new ExecutionReport(Arrays.asList(tc1, tc2));

        String html = reportGenerator.generateHtml(report);

        Assert.assertTrue(html.contains("Total"));
        Assert.assertTrue(html.contains("Passed"));
        Assert.assertTrue(html.contains("Failed"));
        Assert.assertTrue(html.contains("Skipped"));
        Assert.assertTrue(html.contains("Duration"));
    }

    @Test
    public void shouldEscapeHtmlCharacters() {
        TestStep step = new TestStep("Test <script>alert('xss')</script>", new HashMap<>());
        step.setStatus("PASS");
        step.setDurationMs(5L);

        TestCase tc = new TestCase("TC001", "XSS & Test", "Test \"quotes\"");
        tc.addStep(step);
        tc.setStatus("PASS");
        tc.setTotalDurationMs(5L);

        ExecutionReport report = new ExecutionReport(Collections.singletonList(tc));

        String html = reportGenerator.generateHtml(report);

        Assert.assertTrue(html.contains("&lt;script&gt;"));
        Assert.assertTrue(html.contains("&amp;"));
        Assert.assertTrue(html.contains("&quot;quotes&quot;"));
        Assert.assertFalse(html.contains("<script>alert"));
    }

    @Test
    public void shouldHandleEmptyReport() {
        ExecutionReport report = new ExecutionReport(Collections.emptyList());
        String html = reportGenerator.generateHtml(report);
        Assert.assertNotNull(html);
        Assert.assertTrue(html.contains("<!DOCTYPE html>"));
    }

    @Test
    public void shouldHandleNullErrorMessage() {
        TestStep step = new TestStep("Step 1", new HashMap<>());
        step.setStatus("PASS");
        step.setDurationMs(10L);
        step.setErrorMessage(null);

        TestCase tc = new TestCase("TC001", "Test", "Desc");
        tc.addStep(step);
        tc.setStatus("PASS");
        tc.setTotalDurationMs(10L);

        ExecutionReport report = new ExecutionReport(Collections.singletonList(tc));
        String html = reportGenerator.generateHtml(report);
        Assert.assertNotNull(html);
    }

    @Test
    public void shouldHandleEmptyErrorMessage() {
        TestStep step = new TestStep("Step 1", new HashMap<>());
        step.setStatus("PASS");
        step.setDurationMs(10L);
        step.setErrorMessage("");

        TestCase tc = new TestCase("TC001", "Test", "Desc");
        tc.addStep(step);
        tc.setStatus("PASS");
        tc.setTotalDurationMs(10L);

        ExecutionReport report = new ExecutionReport(Collections.singletonList(tc));
        String html = reportGenerator.generateHtml(report);
        Assert.assertNotNull(html);
        Assert.assertFalse(html.contains("<span class='error-msg'>"));
    }

    @Test
    public void shouldHandleNullDescriptionInEscapeHtml() {
        TestCase tc = new TestCase("TC001", null, null);
        tc.setStatus("PASS");
        tc.setTotalDurationMs(0L);
        // executionStatus and executionDate are null, escapeHtml should handle gracefully

        ExecutionReport report = new ExecutionReport(Collections.singletonList(tc));
        String html = reportGenerator.generateHtml(report);
        Assert.assertNotNull(html);
    }

    @Test
    public void shouldEscapeSingleQuotes() {
        TestStep step = new TestStep("Test 'quotes'", new HashMap<>());
        step.setStatus("PASS");
        step.setDurationMs(5L);

        TestCase tc = new TestCase("TC001", "Quote's Test", "Desc");
        tc.addStep(step);
        tc.setStatus("PASS");
        tc.setTotalDurationMs(5L);

        ExecutionReport report = new ExecutionReport(Collections.singletonList(tc));
        String html = reportGenerator.generateHtml(report);
        Assert.assertTrue(html.contains("&#39;"));
    }
}
