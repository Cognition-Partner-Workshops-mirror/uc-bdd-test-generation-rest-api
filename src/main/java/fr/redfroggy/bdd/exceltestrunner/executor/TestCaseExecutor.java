package fr.redfroggy.bdd.exceltestrunner.executor;

import fr.redfroggy.bdd.exceltestrunner.model.TestCase;
import fr.redfroggy.bdd.exceltestrunner.model.TestStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Orchestrates sequential execution of all test cases and their steps,
 * tracking pass/fail/skip status and execution timing.
 *
 * @author Ashish,Raut
 */
@Component
public class TestCaseExecutor {

    private static final Logger logger = LoggerFactory.getLogger(TestCaseExecutor.class);

    // Base URL of the API under test, configured via application properties
    @Value("${exceltestrunner.api.base-url:http://localhost:8080}")
    private String baseUrl;

    // Step executor responsible for executing individual test steps
    private final StepExecutor stepExecutor;

    /**
     * Constructs a TestCaseExecutor with the required StepExecutor dependency.
     *
     * @param stepExecutor the executor for individual test steps
     */
    public TestCaseExecutor(StepExecutor stepExecutor) {
        this.stepExecutor = stepExecutor;
    }

    /**
     * Executes all test cases sequentially. Each test case's steps are executed in order.
     * If a step fails, remaining steps in that test case are marked as SKIP.
     *
     * @param testCases the list of test cases to execute
     */
    public void executeAll(List<TestCase> testCases) {
        for (TestCase testCase : testCases) {
            executeTestCase(testCase);
        }
    }

    /**
     * Executes a single test case by running its steps sequentially.
     * Tracks overall pass/fail status and total execution duration.
     * Steps after a failure are skipped to avoid cascading errors.
     *
     * @param testCase the test case to execute
     */
    private void executeTestCase(TestCase testCase) {
        logger.info("Executing test case: {} - {}", testCase.getTcNumber(), testCase.getTitle());
        long startTime = System.currentTimeMillis();
        boolean hasFailed = false;

        for (TestStep step : testCase.getSteps()) {
            if (hasFailed) {
                // Skip remaining steps after a failure to avoid cascading errors
                step.setStatus("SKIP");
                step.setErrorMessage("Skipped due to previous step failure");
                logger.warn("Skipping step '{}' due to previous failure", step.getDescription());
                continue;
            }

            logger.info("Executing step: {}", step.getDescription());
            stepExecutor.execute(step, baseUrl);

            if ("FAIL".equals(step.getStatus())) {
                hasFailed = true;
                logger.error("Step '{}' failed: {}", step.getDescription(), step.getErrorMessage());
            } else {
                logger.info("Step '{}' passed in {}ms", step.getDescription(), step.getDurationMs());
            }
        }

        // Determine overall test case status based on step results
        testCase.setTotalDurationMs(System.currentTimeMillis() - startTime);
        testCase.setStatus(determineTestCaseStatus(testCase));
        logger.info("Test case {} completed with status: {} in {}ms",
                testCase.getTcNumber(), testCase.getStatus(), testCase.getTotalDurationMs());
    }

    /**
     * Determines the overall test case status based on the individual step results.
     * FAIL if any step failed, PASS if all steps passed, SKIP if all steps were skipped.
     *
     * @param testCase the test case to evaluate
     * @return the overall status string
     */
    private String determineTestCaseStatus(TestCase testCase) {
        boolean anyFail = false;
        boolean anyPass = false;

        for (TestStep step : testCase.getSteps()) {
            if ("FAIL".equals(step.getStatus())) {
                anyFail = true;
            } else if ("PASS".equals(step.getStatus())) {
                anyPass = true;
            }
        }

        if (anyFail) {
            return "FAIL";
        } else if (anyPass) {
            return "PASS";
        } else {
            return "SKIP";
        }
    }

    /**
     * Sets the base URL for testing purposes.
     *
     * @param baseUrl the base API URL
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
