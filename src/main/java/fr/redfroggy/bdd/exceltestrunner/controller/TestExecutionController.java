package fr.redfroggy.bdd.exceltestrunner.controller;

import fr.redfroggy.bdd.exceltestrunner.ExcelTestRunner;
import fr.redfroggy.bdd.exceltestrunner.report.ExecutionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST controller exposing an endpoint to upload an Excel test case file
 * and trigger automated test execution with report generation.
 *
 * @author Ashish,Raut
 */
@RestController
@RequestMapping("/api/excel-test-runner")
public class TestExecutionController {

    private static final Logger logger = LoggerFactory.getLogger(TestExecutionController.class);

    // Main orchestrator for Excel test execution
    private final ExcelTestRunner excelTestRunner;

    /**
     * Constructs the controller with the required ExcelTestRunner dependency.
     *
     * @param excelTestRunner the orchestrator service for test execution
     */
    public TestExecutionController(ExcelTestRunner excelTestRunner) {
        this.excelTestRunner = excelTestRunner;
    }

    /**
     * Accepts an Excel file upload and triggers the full test execution pipeline:
     * parsing, execution, report generation, and email notification.
     *
     * @param file the uploaded Excel (.xlsx) file containing test cases
     * @return a response entity with execution summary or error details
     */
    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeTests(
            @RequestParam("file") MultipartFile file) {

        logger.info("Received test execution request with file: {}", file.getOriginalFilename());

        // Validate the uploaded file is not empty
        if (file.isEmpty()) {
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Uploaded file is empty");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Validate the file has an .xlsx extension
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Only .xlsx files are supported");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            // Execute the full test pipeline and return the report summary
            ExecutionReport report = excelTestRunner.run(file.getInputStream());

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "completed");
            response.put("totalTestCases", report.getTotalCount());
            response.put("passed", report.getPassCount());
            response.put("failed", report.getFailCount());
            response.put("skipped", report.getSkipCount());
            response.put("totalDurationMs", report.getTotalDurationMs());
            response.put("executionTimestamp", report.getExecutionTimestamp().toString());

            logger.info("Test execution completed: {} total, {} passed, {} failed, {} skipped",
                    report.getTotalCount(), report.getPassCount(),
                    report.getFailCount(), report.getSkipCount());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Test execution failed: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Test execution failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
