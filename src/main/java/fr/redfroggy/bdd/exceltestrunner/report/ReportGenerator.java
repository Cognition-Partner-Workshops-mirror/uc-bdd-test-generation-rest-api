package fr.redfroggy.bdd.exceltestrunner.report;

import fr.redfroggy.bdd.exceltestrunner.model.TestCase;
import fr.redfroggy.bdd.exceltestrunner.model.TestStep;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Generates an HTML execution report with summary and per-step details,
 * color-coded by pass/fail/skip status for email delivery.
 *
 * @author Ashish,Raut
 */
@Component
public class ReportGenerator {

    // Date formatter for the report header timestamp
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Generates a complete HTML report from the execution results.
     * The report includes a summary section and per-test-case step details.
     *
     * @param report the ExecutionReport containing all test case results
     * @return the HTML report as a String
     */
    public String generateHtml(ExecutionReport report) {
        StringBuilder html = new StringBuilder();

        // HTML document header with inline CSS styles
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        html.append("<title>Excel Test Runner - Execution Report</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }");
        html.append("h1 { color: #333; border-bottom: 2px solid #4CAF50; padding-bottom: 10px; }");
        html.append("h2 { color: #555; margin-top: 30px; }");
        html.append(".summary { background: #fff; padding: 20px; border-radius: 8px; ");
        html.append("box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-bottom: 20px; }");
        html.append(".summary-item { display: inline-block; margin-right: 30px; text-align: center; }");
        html.append(".summary-count { font-size: 28px; font-weight: bold; }");
        html.append(".summary-label { font-size: 14px; color: #666; }");
        html.append("table { border-collapse: collapse; width: 100%; margin-bottom: 20px; background: #fff; }");
        html.append("th { background-color: #4CAF50; color: white; padding: 12px 8px; text-align: left; }");
        html.append("td { padding: 10px 8px; border-bottom: 1px solid #ddd; }");
        html.append("tr:hover { background-color: #f9f9f9; }");
        html.append(".pass { color: #4CAF50; font-weight: bold; }");
        html.append(".fail { color: #f44336; font-weight: bold; }");
        html.append(".skip { color: #FF9800; font-weight: bold; }");
        html.append(".tc-header { background-color: #e8f5e9; font-weight: bold; }");
        html.append(".error-msg { color: #f44336; font-size: 12px; }");
        html.append("</style></head><body>");

        // Report title and timestamp
        html.append("<h1>Excel Test Runner - Execution Report</h1>");
        html.append("<p>Execution Time: ")
                .append(report.getExecutionTimestamp().format(DATE_FORMATTER))
                .append("</p>");

        // Summary section with pass/fail/skip counts
        html.append("<div class='summary'>");
        appendSummaryItem(html, "Total", report.getTotalCount(), "#333");
        appendSummaryItem(html, "Passed", report.getPassCount(), "#4CAF50");
        appendSummaryItem(html, "Failed", report.getFailCount(), "#f44336");
        appendSummaryItem(html, "Skipped", report.getSkipCount(), "#FF9800");
        html.append("<div class='summary-item'>");
        html.append("<div class='summary-count' style='color:#333;'>")
                .append(report.getTotalDurationMs()).append("ms</div>");
        html.append("<div class='summary-label'>Duration</div>");
        html.append("</div>");
        html.append("</div>");

        // Detailed results table for each test case and its steps
        html.append("<h2>Detailed Results</h2>");
        html.append("<table>");
        html.append("<tr><th>TC#</th><th>Title</th><th>Step</th><th>Status</th>");
        html.append("<th>Duration</th><th>Details</th></tr>");

        for (TestCase testCase : report.getTestCases()) {
            // Test case header row
            html.append("<tr class='tc-header'>");
            html.append("<td>").append(escapeHtml(testCase.getTcNumber())).append("</td>");
            html.append("<td>").append(escapeHtml(testCase.getTitle())).append("</td>");
            html.append("<td>").append(escapeHtml(testCase.getDescription())).append("</td>");
            html.append("<td class='").append(testCase.getStatus().toLowerCase()).append("'>")
                    .append(testCase.getStatus()).append("</td>");
            html.append("<td>").append(testCase.getTotalDurationMs()).append("ms</td>");
            html.append("<td></td>");
            html.append("</tr>");

            // Individual step rows
            for (TestStep step : testCase.getSteps()) {
                html.append("<tr>");
                html.append("<td></td><td></td>");
                html.append("<td>").append(escapeHtml(step.getDescription())).append("</td>");
                html.append("<td class='").append(step.getStatus().toLowerCase()).append("'>")
                        .append(step.getStatus()).append("</td>");
                html.append("<td>").append(step.getDurationMs()).append("ms</td>");
                html.append("<td>");
                if (step.getErrorMessage() != null && !step.getErrorMessage().isEmpty()) {
                    html.append("<span class='error-msg'>")
                            .append(escapeHtml(step.getErrorMessage())).append("</span>");
                }
                html.append("</td>");
                html.append("</tr>");
            }
        }

        html.append("</table>");
        html.append("</body></html>");

        return html.toString();
    }

    /**
     * Appends a summary item block with count and label to the HTML builder.
     *
     * @param html  the StringBuilder to append to
     * @param label the label text (e.g., "Passed", "Failed")
     * @param count the count value to display
     * @param color the CSS color for the count
     */
    private void appendSummaryItem(StringBuilder html, String label, int count, String color) {
        html.append("<div class='summary-item'>");
        html.append("<div class='summary-count' style='color:").append(color).append(";'>")
                .append(count).append("</div>");
        html.append("<div class='summary-label'>").append(label).append("</div>");
        html.append("</div>");
    }

    /**
     * Escapes HTML special characters to prevent XSS in the generated report.
     *
     * @param text the raw text to escape
     * @return the HTML-escaped text
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
