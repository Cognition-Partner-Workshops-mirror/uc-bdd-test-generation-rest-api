package fr.redfroggy.bdd.exceltestrunner.parser;

import fr.redfroggy.bdd.exceltestrunner.exception.InvalidExcelTemplateException;
import fr.redfroggy.bdd.exceltestrunner.model.TestCase;
import fr.redfroggy.bdd.exceltestrunner.model.TestStep;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses and validates Excel test case files using Apache POI. Validates column headers,
 * groups rows by TC#, and builds TestCase objects.
 *
 * @author Ashish,Raut
 */
@Component
public class ExcelParser {

    // Expected column headers in the Excel template (all 7 columns)
    private static final List<String> EXPECTED_HEADERS = Arrays.asList(
            "TC#", "Title", "Description", "Steps", "Data", "Execution status", "Execution Date"
    );

    /**
     * Parses the uploaded Excel file and returns a list of TestCase objects.
     * Rows are grouped by TC# to form distinct test cases, each containing one or more steps.
     *
     * @param inputStream the input stream of the uploaded Excel file
     * @return ordered list of TestCase objects parsed from the spreadsheet
     * @throws IOException                   if the file cannot be read
     * @throws InvalidExcelTemplateException if the column headers do not match the expected template
     */
    public List<TestCase> parse(InputStream inputStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            // Validate column headers on the first row
            validateHeaders(sheet.getRow(0));

            // Group rows by TC# to build test cases
            return buildTestCases(sheet);
        }
    }

    /**
     * Validates that the first row contains the expected column headers.
     *
     * @param headerRow the first row of the spreadsheet
     * @throws InvalidExcelTemplateException if headers are missing or do not match
     */
    private void validateHeaders(Row headerRow) {
        if (headerRow == null) {
            throw new InvalidExcelTemplateException("Excel file is empty, no header row found.");
        }

        List<String> actualHeaders = new ArrayList<>();
        for (int i = 0; i < EXPECTED_HEADERS.size(); i++) {
            Cell cell = headerRow.getCell(i);
            String headerValue = getCellValueAsString(cell);
            actualHeaders.add(headerValue);
        }

        // Check each expected header is present in the correct position
        for (int i = 0; i < EXPECTED_HEADERS.size(); i++) {
            String expected = EXPECTED_HEADERS.get(i);
            String actual = i < actualHeaders.size() ? actualHeaders.get(i) : "";
            if (!expected.equalsIgnoreCase(actual.trim())) {
                throw new InvalidExcelTemplateException(
                        String.format("Invalid column header at position %d: expected '%s' but found '%s'. "
                                + "Expected headers: %s", i, expected, actual, EXPECTED_HEADERS));
            }
        }
    }

    /**
     * Iterates over data rows (skipping header), groups them by TC#, and builds TestCase objects.
     * Rows with the same TC# are combined into one TestCase with multiple TestSteps.
     *
     * @param sheet the Excel sheet to parse
     * @return list of parsed TestCase objects
     */
    private List<TestCase> buildTestCases(Sheet sheet) {
        // Use LinkedHashMap to preserve insertion order of test cases
        Map<String, TestCase> testCaseMap = new LinkedHashMap<>();

        for (int rowIdx = 1; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
            Row row = sheet.getRow(rowIdx);
            if (row == null) {
                continue;
            }

            String tcNumber = getCellValueAsString(row.getCell(0)).trim();
            String title = getCellValueAsString(row.getCell(1)).trim();
            String description = getCellValueAsString(row.getCell(2)).trim();
            String stepDescription = getCellValueAsString(row.getCell(3)).trim();
            String dataColumn = getCellValueAsString(row.getCell(4)).trim();
            // Read Execution status and Execution Date columns (columns 5 and 6)
            String executionStatus = getCellValueAsString(row.getCell(5)).trim();
            String executionDate = getCellValueAsString(row.getCell(6)).trim();

            // Skip completely empty rows
            if (tcNumber.isEmpty() && stepDescription.isEmpty()) {
                continue;
            }

            // Get or create test case for this TC#
            TestCase testCase = testCaseMap.get(tcNumber);
            if (testCase == null) {
                testCase = new TestCase(tcNumber, title, description);
                // Set execution status and date from the first row of this test case
                testCase.setExecutionStatus(executionStatus);
                testCase.setExecutionDate(executionDate);
                testCaseMap.put(tcNumber, testCase);
            }

            // Parse data column into key-value pairs and create a test step
            if (!stepDescription.isEmpty()) {
                Map<String, String> parsedData = DataColumnParser.parse(dataColumn);
                TestStep step = new TestStep(stepDescription, parsedData);
                testCase.addStep(step);
            }
        }

        return new ArrayList<>(testCaseMap.values());
    }

    /**
     * Safely extracts the string value from a cell, handling null and numeric types.
     *
     * @param cell the Excel cell to read
     * @return the cell value as a String, or empty string if null
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            // Convert numeric values to string without decimal points
            double numericValue = cell.getNumericCellValue();
            if (numericValue == Math.floor(numericValue)) {
                return String.valueOf((long) numericValue);
            }
            return String.valueOf(numericValue);
        }
        if (cell.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        }
        return cell.getStringCellValue();
    }
}
