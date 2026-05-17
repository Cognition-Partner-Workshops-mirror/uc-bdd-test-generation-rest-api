package fr.redfroggy.bdd.exceltestrunner.parser;

import fr.redfroggy.bdd.exceltestrunner.exception.InvalidExcelTemplateException;
import fr.redfroggy.bdd.exceltestrunner.model.TestCase;
import fr.redfroggy.bdd.exceltestrunner.model.TestStep;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExcelParserTest {

    private ExcelParser excelParser;

    @Before
    public void setUp() {
        excelParser = new ExcelParser();
    }

    @Test
    public void shouldParseValidExcelFile() throws IOException {
        byte[] excelBytes = createValidExcelFile();
        List<TestCase> testCases = excelParser.parse(new ByteArrayInputStream(excelBytes));
        Assert.assertNotNull(testCases);
        Assert.assertEquals(1, testCases.size());

        TestCase tc = testCases.get(0);
        Assert.assertEquals("TC001", tc.getTcNumber());
        Assert.assertEquals("User Creation", tc.getTitle());
        Assert.assertEquals("Create a new user via API", tc.getDescription());
        // Verify Execution status and Execution Date columns are parsed
        Assert.assertEquals("Pending", tc.getExecutionStatus());
        Assert.assertEquals("2026-01-15", tc.getExecutionDate());
        Assert.assertEquals(2, tc.getSteps().size());

        // Verify Data column is parsed into key-value pairs for JSON preparation
        TestStep step1 = tc.getSteps().get(0);
        Assert.assertEquals("Create a new user", step1.getDescription());
        Assert.assertEquals("john", step1.getData().get("username"));
        Assert.assertEquals("john@test.com", step1.getData().get("email"));
        Assert.assertEquals("secret", step1.getData().get("password"));
    }

    @Test
    public void shouldGroupRowsByTcNumber() throws IOException {
        byte[] excelBytes = createMultipleTcExcelFile();
        List<TestCase> testCases = excelParser.parse(new ByteArrayInputStream(excelBytes));
        Assert.assertEquals(2, testCases.size());
        Assert.assertEquals("TC001", testCases.get(0).getTcNumber());
        Assert.assertEquals("TC002", testCases.get(1).getTcNumber());
    }

    @Test(expected = InvalidExcelTemplateException.class)
    public void shouldThrowExceptionForInvalidHeaders() throws IOException {
        byte[] excelBytes = createInvalidHeaderExcelFile();
        excelParser.parse(new ByteArrayInputStream(excelBytes));
    }

    @Test(expected = InvalidExcelTemplateException.class)
    public void shouldThrowExceptionForEmptyFile() throws IOException {
        byte[] excelBytes = createEmptyExcelFile();
        excelParser.parse(new ByteArrayInputStream(excelBytes));
    }

    @Test
    public void shouldSkipEmptyRows() throws IOException {
        byte[] excelBytes = createExcelFileWithEmptyRows();
        List<TestCase> testCases = excelParser.parse(new ByteArrayInputStream(excelBytes));
        Assert.assertEquals(1, testCases.size());
        Assert.assertEquals(1, testCases.get(0).getSteps().size());
    }

    @Test
    public void shouldHandleNumericCellValues() throws IOException {
        byte[] excelBytes = createExcelFileWithNumericTc();
        List<TestCase> testCases = excelParser.parse(new ByteArrayInputStream(excelBytes));
        Assert.assertFalse(testCases.isEmpty());
    }

    @Test
    public void shouldHandleBooleanCellValues() throws IOException {
        byte[] excelBytes = createExcelFileWithBooleanCell();
        List<TestCase> testCases = excelParser.parse(new ByteArrayInputStream(excelBytes));
        Assert.assertNotNull(testCases);
    }

    @Test
    public void shouldHandleStepWithEmptyDataColumn() throws IOException {
        byte[] excelBytes = createExcelFileWithEmptyData();
        List<TestCase> testCases = excelParser.parse(new ByteArrayInputStream(excelBytes));
        Assert.assertEquals(1, testCases.size());
        Assert.assertEquals(1, testCases.get(0).getSteps().size());
        Assert.assertTrue(testCases.get(0).getSteps().get(0).getData().isEmpty());
    }

    @Test
    public void shouldHandleDecimalNumericCellValues() throws IOException {
        byte[] excelBytes = createExcelFileWithDecimalNumericTc();
        List<TestCase> testCases = excelParser.parse(new ByteArrayInputStream(excelBytes));
        Assert.assertFalse(testCases.isEmpty());
    }

    @Test
    public void shouldParseExecutionStatusAndDateColumns() throws IOException {
        // Verify that Execution status and Execution Date are read from the Excel template
        byte[] excelBytes = createValidExcelFile();
        List<TestCase> testCases = excelParser.parse(new ByteArrayInputStream(excelBytes));
        TestCase tc = testCases.get(0);
        Assert.assertEquals("Pending", tc.getExecutionStatus());
        Assert.assertEquals("2026-01-15", tc.getExecutionDate());
    }

    @Test
    public void shouldHandleEmptyExecutionStatusAndDate() throws IOException {
        // Verify parsing works when Execution status and Execution Date are empty
        byte[] excelBytes = createExcelFileWithEmptyExecutionColumns();
        List<TestCase> testCases = excelParser.parse(new ByteArrayInputStream(excelBytes));
        Assert.assertEquals(1, testCases.size());
        Assert.assertEquals("", testCases.get(0).getExecutionStatus());
        Assert.assertEquals("", testCases.get(0).getExecutionDate());
    }

    @Test
    public void shouldParseDataColumnIntoKeyValuePairs() throws IOException {
        // Verify that the Data column (key:'value' pairs) is parsed for JSON preparation
        byte[] excelBytes = createValidExcelFile();
        List<TestCase> testCases = excelParser.parse(new ByteArrayInputStream(excelBytes));
        TestStep step1 = testCases.get(0).getSteps().get(0);
        Map<String, String> data = step1.getData();

        // Data column: "username:'john', email:'john@test.com', password:'secret'"
        Assert.assertEquals(3, data.size());
        Assert.assertEquals("john", data.get("username"));
        Assert.assertEquals("john@test.com", data.get("email"));
        Assert.assertEquals("secret", data.get("password"));
    }

    @Test
    public void shouldParseDataColumnWithSingleField() throws IOException {
        // Verify Data column parsing with a single key-value pair for endpoint URL
        byte[] excelBytes = createValidExcelFile();
        List<TestCase> testCases = excelParser.parse(new ByteArrayInputStream(excelBytes));
        TestStep step2 = testCases.get(0).getSteps().get(1);
        Map<String, String> data = step2.getData();

        // Data column: "userId:'1'"
        Assert.assertEquals(1, data.size());
        Assert.assertEquals("1", data.get("userId"));
    }

    @Test(expected = InvalidExcelTemplateException.class)
    public void shouldRejectMissingExecutionStatusHeader() throws IOException {
        // Verify that missing 'Execution status' header is rejected
        byte[] excelBytes = createExcelFileMissingExecutionHeaders();
        excelParser.parse(new ByteArrayInputStream(excelBytes));
    }

    @Test
    public void shouldParseMultipleTcsWithDifferentExecutionDates() throws IOException {
        // Verify each TC gets its own execution status and date
        byte[] excelBytes = createMultipleTcExcelFile();
        List<TestCase> testCases = excelParser.parse(new ByteArrayInputStream(excelBytes));
        Assert.assertEquals("Pending", testCases.get(0).getExecutionStatus());
        Assert.assertEquals("2026-01-15", testCases.get(0).getExecutionDate());
        Assert.assertEquals("Completed", testCases.get(1).getExecutionStatus());
        Assert.assertEquals("2026-01-16", testCases.get(1).getExecutionDate());
    }

    // Helper: creates a standard 7-column header row
    private void createStandardHeaders(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("TC#");
        header.createCell(1).setCellValue("Title");
        header.createCell(2).setCellValue("Description");
        header.createCell(3).setCellValue("Steps");
        header.createCell(4).setCellValue("Data");
        header.createCell(5).setCellValue("Execution status");
        header.createCell(6).setCellValue("Execution Date");
    }

    // Helper: creates a valid Excel file with all 7 column headers and data
    private byte[] createValidExcelFile() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test Cases");
        createStandardHeaders(sheet);

        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("TC001");
        row1.createCell(1).setCellValue("User Creation");
        row1.createCell(2).setCellValue("Create a new user via API");
        row1.createCell(3).setCellValue("Create a new user");
        row1.createCell(4).setCellValue("username:'john', email:'john@test.com', password:'secret'");
        row1.createCell(5).setCellValue("Pending");
        row1.createCell(6).setCellValue("2026-01-15");

        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue("TC001");
        row2.createCell(1).setCellValue("User Creation");
        row2.createCell(2).setCellValue("Create a new user via API");
        row2.createCell(3).setCellValue("Get user by ID");
        row2.createCell(4).setCellValue("userId:'1'");
        row2.createCell(5).setCellValue("");
        row2.createCell(6).setCellValue("");

        return workbookToBytes(workbook);
    }

    // Helper: creates an Excel file with multiple test cases
    private byte[] createMultipleTcExcelFile() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test Cases");
        createStandardHeaders(sheet);

        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("TC001");
        row1.createCell(1).setCellValue("Test 1");
        row1.createCell(2).setCellValue("Desc 1");
        row1.createCell(3).setCellValue("Create a new user");
        row1.createCell(4).setCellValue("username:'a', email:'a@t.com', password:'p'");
        row1.createCell(5).setCellValue("Pending");
        row1.createCell(6).setCellValue("2026-01-15");

        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue("TC002");
        row2.createCell(1).setCellValue("Test 2");
        row2.createCell(2).setCellValue("Desc 2");
        row2.createCell(3).setCellValue("List all users");
        row2.createCell(4).setCellValue("");
        row2.createCell(5).setCellValue("Completed");
        row2.createCell(6).setCellValue("2026-01-16");

        return workbookToBytes(workbook);
    }

    // Helper: creates an Excel file with invalid headers
    private byte[] createInvalidHeaderExcelFile() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test Cases");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Wrong");
        header.createCell(1).setCellValue("Headers");

        return workbookToBytes(workbook);
    }

    // Helper: creates an empty Excel file (no rows)
    private byte[] createEmptyExcelFile() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        workbook.createSheet("Test Cases");
        return workbookToBytes(workbook);
    }

    // Helper: creates an Excel file with empty rows between data
    private byte[] createExcelFileWithEmptyRows() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test Cases");
        createStandardHeaders(sheet);

        // Empty row at index 1 (null row)
        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue("TC001");
        row2.createCell(1).setCellValue("Test");
        row2.createCell(2).setCellValue("Desc");
        row2.createCell(3).setCellValue("List all users");
        row2.createCell(4).setCellValue("");
        row2.createCell(5).setCellValue("");
        row2.createCell(6).setCellValue("");

        return workbookToBytes(workbook);
    }

    // Helper: creates an Excel file with numeric TC# values
    private byte[] createExcelFileWithNumericTc() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test Cases");
        createStandardHeaders(sheet);

        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue(1);
        row1.createCell(1).setCellValue("Numeric TC");
        row1.createCell(2).setCellValue("Test numeric");
        row1.createCell(3).setCellValue("List all users");
        row1.createCell(4).setCellValue("");
        row1.createCell(5).setCellValue("");
        row1.createCell(6).setCellValue("");

        return workbookToBytes(workbook);
    }

    // Helper: creates an Excel file with a boolean cell value
    private byte[] createExcelFileWithBooleanCell() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test Cases");
        createStandardHeaders(sheet);

        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("TC001");
        row1.createCell(1).setCellValue(true);
        row1.createCell(2).setCellValue("Test boolean");
        row1.createCell(3).setCellValue("List all users");
        row1.createCell(4).setCellValue("");
        row1.createCell(5).setCellValue("");
        row1.createCell(6).setCellValue("");

        return workbookToBytes(workbook);
    }

    // Helper: creates an Excel file with empty Data column
    private byte[] createExcelFileWithEmptyData() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test Cases");
        createStandardHeaders(sheet);

        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("TC001");
        row1.createCell(1).setCellValue("Test");
        row1.createCell(2).setCellValue("Desc");
        row1.createCell(3).setCellValue("List all users");
        row1.createCell(4).setCellValue("");
        row1.createCell(5).setCellValue("");
        row1.createCell(6).setCellValue("");

        return workbookToBytes(workbook);
    }

    // Helper: creates an Excel file with decimal numeric TC# values
    private byte[] createExcelFileWithDecimalNumericTc() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test Cases");
        createStandardHeaders(sheet);

        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue(1.5);
        row1.createCell(1).setCellValue("Decimal TC");
        row1.createCell(2).setCellValue("Test decimal");
        row1.createCell(3).setCellValue("List all users");
        row1.createCell(4).setCellValue("");
        row1.createCell(5).setCellValue("");
        row1.createCell(6).setCellValue("");

        return workbookToBytes(workbook);
    }

    // Helper: creates an Excel file with empty Execution status and Execution Date
    private byte[] createExcelFileWithEmptyExecutionColumns() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test Cases");
        createStandardHeaders(sheet);

        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("TC001");
        row1.createCell(1).setCellValue("Test");
        row1.createCell(2).setCellValue("Desc");
        row1.createCell(3).setCellValue("Step 1");
        row1.createCell(4).setCellValue("field:'value'");
        row1.createCell(5).setCellValue("");
        row1.createCell(6).setCellValue("");

        return workbookToBytes(workbook);
    }

    // Helper: creates an Excel file with only 5 columns (missing Execution status/Date)
    private byte[] createExcelFileMissingExecutionHeaders() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test Cases");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("TC#");
        header.createCell(1).setCellValue("Title");
        header.createCell(2).setCellValue("Description");
        header.createCell(3).setCellValue("Steps");
        header.createCell(4).setCellValue("Data");
        // Missing: Execution status (col 5) and Execution Date (col 6)

        return workbookToBytes(workbook);
    }

    private byte[] workbookToBytes(Workbook workbook) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        return bos.toByteArray();
    }
}
