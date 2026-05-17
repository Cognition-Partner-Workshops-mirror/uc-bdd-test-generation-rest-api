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
        Assert.assertEquals(2, tc.getSteps().size());

        TestStep step1 = tc.getSteps().get(0);
        Assert.assertEquals("Create a new user", step1.getDescription());
        Assert.assertEquals("john", step1.getData().get("username"));
        Assert.assertEquals("john@test.com", step1.getData().get("email"));
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

    // Helper: creates a valid Excel file with proper headers and data
    private byte[] createValidExcelFile() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test Cases");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("TC#");
        header.createCell(1).setCellValue("Title");
        header.createCell(2).setCellValue("Description");
        header.createCell(3).setCellValue("Steps");
        header.createCell(4).setCellValue("Data");

        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("TC001");
        row1.createCell(1).setCellValue("User Creation");
        row1.createCell(2).setCellValue("Create a new user via API");
        row1.createCell(3).setCellValue("Create a new user");
        row1.createCell(4).setCellValue("username:'john', email:'john@test.com', password:'secret'");

        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue("TC001");
        row2.createCell(1).setCellValue("User Creation");
        row2.createCell(2).setCellValue("Create a new user via API");
        row2.createCell(3).setCellValue("Get user by ID");
        row2.createCell(4).setCellValue("userId:'1'");

        return workbookToBytes(workbook);
    }

    // Helper: creates an Excel file with multiple test cases
    private byte[] createMultipleTcExcelFile() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test Cases");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("TC#");
        header.createCell(1).setCellValue("Title");
        header.createCell(2).setCellValue("Description");
        header.createCell(3).setCellValue("Steps");
        header.createCell(4).setCellValue("Data");

        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("TC001");
        row1.createCell(1).setCellValue("Test 1");
        row1.createCell(2).setCellValue("Desc 1");
        row1.createCell(3).setCellValue("Create a new user");
        row1.createCell(4).setCellValue("username:'a', email:'a@t.com', password:'p'");

        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue("TC002");
        row2.createCell(1).setCellValue("Test 2");
        row2.createCell(2).setCellValue("Desc 2");
        row2.createCell(3).setCellValue("List all users");
        row2.createCell(4).setCellValue("");

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

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("TC#");
        header.createCell(1).setCellValue("Title");
        header.createCell(2).setCellValue("Description");
        header.createCell(3).setCellValue("Steps");
        header.createCell(4).setCellValue("Data");

        // Empty row at index 1 (null row)
        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue("TC001");
        row2.createCell(1).setCellValue("Test");
        row2.createCell(2).setCellValue("Desc");
        row2.createCell(3).setCellValue("List all users");
        row2.createCell(4).setCellValue("");

        return workbookToBytes(workbook);
    }

    // Helper: creates an Excel file with numeric TC# values
    private byte[] createExcelFileWithNumericTc() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test Cases");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("TC#");
        header.createCell(1).setCellValue("Title");
        header.createCell(2).setCellValue("Description");
        header.createCell(3).setCellValue("Steps");
        header.createCell(4).setCellValue("Data");

        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue(1);
        row1.createCell(1).setCellValue("Numeric TC");
        row1.createCell(2).setCellValue("Test numeric");
        row1.createCell(3).setCellValue("List all users");
        row1.createCell(4).setCellValue("");

        return workbookToBytes(workbook);
    }

    // Helper: creates an Excel file with a boolean cell value
    private byte[] createExcelFileWithBooleanCell() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test Cases");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("TC#");
        header.createCell(1).setCellValue("Title");
        header.createCell(2).setCellValue("Description");
        header.createCell(3).setCellValue("Steps");
        header.createCell(4).setCellValue("Data");

        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("TC001");
        row1.createCell(1).setCellValue(true);
        row1.createCell(2).setCellValue("Test boolean");
        row1.createCell(3).setCellValue("List all users");
        row1.createCell(4).setCellValue("");

        return workbookToBytes(workbook);
    }

    // Helper: creates an Excel file with empty Data column
    private byte[] createExcelFileWithEmptyData() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test Cases");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("TC#");
        header.createCell(1).setCellValue("Title");
        header.createCell(2).setCellValue("Description");
        header.createCell(3).setCellValue("Steps");
        header.createCell(4).setCellValue("Data");

        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("TC001");
        row1.createCell(1).setCellValue("Test");
        row1.createCell(2).setCellValue("Desc");
        row1.createCell(3).setCellValue("List all users");
        row1.createCell(4).setCellValue("");

        return workbookToBytes(workbook);
    }

    // Helper: creates an Excel file with decimal numeric TC# values
    private byte[] createExcelFileWithDecimalNumericTc() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test Cases");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("TC#");
        header.createCell(1).setCellValue("Title");
        header.createCell(2).setCellValue("Description");
        header.createCell(3).setCellValue("Steps");
        header.createCell(4).setCellValue("Data");

        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue(1.5);
        row1.createCell(1).setCellValue("Decimal TC");
        row1.createCell(2).setCellValue("Test decimal");
        row1.createCell(3).setCellValue("List all users");
        row1.createCell(4).setCellValue("");

        return workbookToBytes(workbook);
    }

    private byte[] workbookToBytes(Workbook workbook) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        return bos.toByteArray();
    }
}
