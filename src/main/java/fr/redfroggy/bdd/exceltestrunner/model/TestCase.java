package fr.redfroggy.bdd.exceltestrunner.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a test case parsed from the Excel file,
 * containing TC number, title, description, and a list of test steps.
 *
 * @author Ashish,Raut
 */
public class TestCase {

    // Test case identifier from the TC# column (e.g., "TC001")
    private String tcNumber;

    // Title of the test case from the Title column
    private String title;

    // Description of the test case from the Description column
    private String description;

    // Ordered list of test steps belonging to this test case
    private List<TestStep> steps;

    // Overall status of the test case: PASS, FAIL, or SKIP
    private String status;

    // Execution status from the Excel file (user-provided initial status)
    private String executionStatus;

    // Execution date from the Excel file (user-provided date of execution)
    private String executionDate;

    // Total execution duration for all steps in milliseconds
    private long totalDurationMs;

    /**
     * Default constructor initializing the steps list and pending status.
     */
    public TestCase() {
        this.steps = new ArrayList<>();
        this.status = "PENDING";
    }

    /**
     * Constructs a TestCase with the specified TC number, title, and description.
     *
     * @param tcNumber    the test case identifier
     * @param title       the test case title
     * @param description the test case description
     */
    public TestCase(String tcNumber, String title, String description) {
        this.tcNumber = tcNumber;
        this.title = title;
        this.description = description;
        this.steps = new ArrayList<>();
        this.status = "PENDING";
    }

    /**
     * Adds a test step to this test case.
     *
     * @param step the TestStep to add
     */
    public void addStep(TestStep step) {
        this.steps.add(step);
    }

    public String getTcNumber() {
        return tcNumber;
    }

    public void setTcNumber(String tcNumber) {
        this.tcNumber = tcNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<TestStep> getSteps() {
        return steps;
    }

    public void setSteps(List<TestStep> steps) {
        this.steps = steps;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTotalDurationMs() {
        return totalDurationMs;
    }

    public void setTotalDurationMs(long totalDurationMs) {
        this.totalDurationMs = totalDurationMs;
    }

    public String getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(String executionStatus) {
        this.executionStatus = executionStatus;
    }

    public String getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(String executionDate) {
        this.executionDate = executionDate;
    }
}
