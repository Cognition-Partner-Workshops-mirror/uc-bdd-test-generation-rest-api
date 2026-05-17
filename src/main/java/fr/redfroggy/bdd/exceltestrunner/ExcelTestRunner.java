package fr.redfroggy.bdd.exceltestrunner;

import fr.redfroggy.bdd.exceltestrunner.executor.TestCaseExecutor;
import fr.redfroggy.bdd.exceltestrunner.model.TestCase;
import fr.redfroggy.bdd.exceltestrunner.parser.ExcelParser;
import fr.redfroggy.bdd.exceltestrunner.report.EmailReportSender;
import fr.redfroggy.bdd.exceltestrunner.report.ExecutionReport;
import fr.redfroggy.bdd.exceltestrunner.report.ReportGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Main orchestrator service that coordinates Excel parsing, test execution,
 * report generation, and email notification.
 *
 * @author Ashish,Raut
 */
@Service
public class ExcelTestRunner {

    private static final Logger logger = LoggerFactory.getLogger(ExcelTestRunner.class);

    // Date formatter for the email subject timestamp
    private static final DateTimeFormatter SUBJECT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Parser for reading and validating Excel test case files
    private final ExcelParser excelParser;

    // Executor for running test cases against the target API
    private final TestCaseExecutor testCaseExecutor;

    // Generator for creating HTML execution reports
    private final ReportGenerator reportGenerator;

    // Sender for emailing the execution report
    private final EmailReportSender emailReportSender;

    /**
     * Constructs the ExcelTestRunner with all required dependencies.
     *
     * @param excelParser       the Excel file parser
     * @param testCaseExecutor  the test case executor
     * @param reportGenerator   the HTML report generator
     * @param emailReportSender the email report sender
     */
    public ExcelTestRunner(ExcelParser excelParser,
                           TestCaseExecutor testCaseExecutor,
                           ReportGenerator reportGenerator,
                           EmailReportSender emailReportSender) {
        this.excelParser = excelParser;
        this.testCaseExecutor = testCaseExecutor;
        this.reportGenerator = reportGenerator;
        this.emailReportSender = emailReportSender;
    }

    /**
     * Runs the full test execution pipeline:
     * 1. Parses the Excel file into test cases
     * 2. Executes all test cases sequentially
     * 3. Generates an HTML execution report
     * 4. Sends the report via email
     *
     * @param excelInputStream the input stream of the uploaded Excel file
     * @return the ExecutionReport containing all results and summary
     * @throws IOException if the Excel file cannot be read
     */
    public ExecutionReport run(InputStream excelInputStream) throws IOException {
        logger.info("Starting Excel test execution pipeline");

        // Step 1: Parse the Excel file into test cases
        logger.info("Step 1: Parsing Excel file");
        List<TestCase> testCases = excelParser.parse(excelInputStream);
        logger.info("Parsed {} test cases from Excel file", testCases.size());

        // Step 2: Execute all test cases
        logger.info("Step 2: Executing test cases");
        testCaseExecutor.executeAll(testCases);

        // Step 3: Build the execution report
        logger.info("Step 3: Generating execution report");
        ExecutionReport report = new ExecutionReport(testCases);

        // Step 4: Generate HTML report and send via email
        logger.info("Step 4: Generating HTML report and sending email");
        String htmlReport = reportGenerator.generateHtml(report);
        String subject = String.format("Excel Test Runner Report - %s [%d/%d passed]",
                report.getExecutionTimestamp().format(SUBJECT_DATE_FORMATTER),
                report.getPassCount(), report.getTotalCount());

        try {
            emailReportSender.sendReport(htmlReport, subject);
        } catch (Exception e) {
            // Log email failure but do not fail the entire execution
            logger.warn("Failed to send email report, but execution completed: {}", e.getMessage());
        }

        logger.info("Excel test execution pipeline completed: {} total, {} passed, {} failed, {} skipped",
                report.getTotalCount(), report.getPassCount(),
                report.getFailCount(), report.getSkipCount());

        return report;
    }
}
