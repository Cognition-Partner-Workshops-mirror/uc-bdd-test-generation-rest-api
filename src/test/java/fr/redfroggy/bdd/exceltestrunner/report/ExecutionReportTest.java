package fr.redfroggy.bdd.exceltestrunner.report;

import fr.redfroggy.bdd.exceltestrunner.model.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExecutionReportTest {

    @Test
    public void shouldCreateWithDefaultConstructor() {
        ExecutionReport report = new ExecutionReport();
        Assert.assertNotNull(report.getTestCases());
        Assert.assertTrue(report.getTestCases().isEmpty());
        Assert.assertNotNull(report.getExecutionTimestamp());
        Assert.assertEquals(0, report.getPassCount());
        Assert.assertEquals(0, report.getFailCount());
        Assert.assertEquals(0, report.getSkipCount());
        Assert.assertEquals(0, report.getTotalCount());
        Assert.assertEquals(0L, report.getTotalDurationMs());
    }

    @Test
    public void shouldCalculateSummaryFromTestCases() {
        TestCase tc1 = new TestCase("TC001", "Pass Test", "");
        tc1.setStatus("PASS");
        tc1.setTotalDurationMs(100L);

        TestCase tc2 = new TestCase("TC002", "Fail Test", "");
        tc2.setStatus("FAIL");
        tc2.setTotalDurationMs(200L);

        TestCase tc3 = new TestCase("TC003", "Skip Test", "");
        tc3.setStatus("SKIP");
        tc3.setTotalDurationMs(50L);

        ExecutionReport report = new ExecutionReport(Arrays.asList(tc1, tc2, tc3));

        Assert.assertEquals(3, report.getTotalCount());
        Assert.assertEquals(1, report.getPassCount());
        Assert.assertEquals(1, report.getFailCount());
        Assert.assertEquals(1, report.getSkipCount());
        Assert.assertEquals(350L, report.getTotalDurationMs());
    }

    @Test
    public void shouldHandleEmptyTestCases() {
        ExecutionReport report = new ExecutionReport(new ArrayList<>());
        Assert.assertEquals(0, report.getTotalCount());
        Assert.assertEquals(0, report.getPassCount());
        Assert.assertEquals(0, report.getFailCount());
        Assert.assertEquals(0, report.getSkipCount());
    }

    @Test
    public void shouldSetAndGetAllFields() {
        ExecutionReport report = new ExecutionReport();
        LocalDateTime timestamp = LocalDateTime.of(2024, 1, 1, 12, 0);
        report.setExecutionTimestamp(timestamp);
        report.setPassCount(5);
        report.setFailCount(2);
        report.setSkipCount(1);
        report.setTotalCount(8);
        report.setTotalDurationMs(1000L);

        List<TestCase> testCases = new ArrayList<>();
        testCases.add(new TestCase("TC001", "Test", ""));
        report.setTestCases(testCases);

        Assert.assertEquals(timestamp, report.getExecutionTimestamp());
        Assert.assertEquals(5, report.getPassCount());
        Assert.assertEquals(2, report.getFailCount());
        Assert.assertEquals(1, report.getSkipCount());
        Assert.assertEquals(8, report.getTotalCount());
        Assert.assertEquals(1000L, report.getTotalDurationMs());
        Assert.assertEquals(1, report.getTestCases().size());
    }

    @Test
    public void shouldRecalculateSummary() {
        ExecutionReport report = new ExecutionReport();
        TestCase tc = new TestCase("TC001", "Test", "");
        tc.setStatus("PASS");
        tc.setTotalDurationMs(100L);

        List<TestCase> testCases = new ArrayList<>();
        testCases.add(tc);
        report.setTestCases(testCases);

        report.calculateSummary();
        Assert.assertEquals(1, report.getTotalCount());
        Assert.assertEquals(1, report.getPassCount());
        Assert.assertEquals(0, report.getFailCount());
        Assert.assertEquals(100L, report.getTotalDurationMs());
    }

    @Test
    public void shouldHandleUnknownStatus() {
        TestCase tc = new TestCase("TC001", "Test", "");
        tc.setStatus("PENDING");
        tc.setTotalDurationMs(50L);

        ExecutionReport report = new ExecutionReport(Arrays.asList(tc));
        Assert.assertEquals(1, report.getTotalCount());
        Assert.assertEquals(0, report.getPassCount());
        Assert.assertEquals(0, report.getFailCount());
        Assert.assertEquals(0, report.getSkipCount());
    }
}
