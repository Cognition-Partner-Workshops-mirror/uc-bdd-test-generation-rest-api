package fr.redfroggy.bdd.exceltestrunner.report;

import fr.redfroggy.bdd.exceltestrunner.model.TestCase;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * POJO holding the complete execution results including per-TC details,
 * overall summary counts, and execution timestamp.
 *
 * @author Ashish,Raut
 */
public class ExecutionReport {

    // Timestamp when the execution started
    private LocalDateTime executionTimestamp;

    // List of all executed test cases with their results
    private List<TestCase> testCases;

    // Count of test cases that passed
    private int passCount;

    // Count of test cases that failed
    private int failCount;

    // Count of test cases that were skipped
    private int skipCount;

    // Total number of test cases executed
    private int totalCount;

    // Total execution duration in milliseconds
    private long totalDurationMs;

    /**
     * Default constructor initializing the test cases list and execution timestamp.
     */
    public ExecutionReport() {
        this.testCases = new ArrayList<>();
        this.executionTimestamp = LocalDateTime.now();
    }

    /**
     * Constructs an ExecutionReport from the list of executed test cases.
     * Automatically calculates summary counts from the test case statuses.
     *
     * @param testCases the list of executed test cases
     */
    public ExecutionReport(List<TestCase> testCases) {
        this.testCases = testCases;
        this.executionTimestamp = LocalDateTime.now();
        calculateSummary();
    }

    /**
     * Calculates pass/fail/skip counts and total duration from the test case list.
     */
    public void calculateSummary() {
        this.totalCount = testCases.size();
        this.passCount = 0;
        this.failCount = 0;
        this.skipCount = 0;
        this.totalDurationMs = 0;

        for (TestCase tc : testCases) {
            switch (tc.getStatus()) {
                case "PASS":
                    passCount++;
                    break;
                case "FAIL":
                    failCount++;
                    break;
                case "SKIP":
                    skipCount++;
                    break;
                default:
                    break;
            }
            totalDurationMs += tc.getTotalDurationMs();
        }
    }

    public LocalDateTime getExecutionTimestamp() {
        return executionTimestamp;
    }

    public void setExecutionTimestamp(LocalDateTime executionTimestamp) {
        this.executionTimestamp = executionTimestamp;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases;
    }

    public int getPassCount() {
        return passCount;
    }

    public void setPassCount(int passCount) {
        this.passCount = passCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public int getSkipCount() {
        return skipCount;
    }

    public void setSkipCount(int skipCount) {
        this.skipCount = skipCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public long getTotalDurationMs() {
        return totalDurationMs;
    }

    public void setTotalDurationMs(long totalDurationMs) {
        this.totalDurationMs = totalDurationMs;
    }
}
