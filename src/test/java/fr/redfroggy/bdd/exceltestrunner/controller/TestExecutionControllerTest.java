package fr.redfroggy.bdd.exceltestrunner.controller;

import fr.redfroggy.bdd.exceltestrunner.ExcelTestRunner;
import fr.redfroggy.bdd.exceltestrunner.report.ExecutionReport;
import fr.redfroggy.bdd.exceltestrunner.model.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TestExecutionControllerTest {

    private ExcelTestRunner excelTestRunner;
    private TestExecutionController controller;

    @Before
    public void setUp() {
        excelTestRunner = Mockito.mock(ExcelTestRunner.class);
        controller = new TestExecutionController(excelTestRunner);
    }

    @Test
    public void shouldExecuteTestsSuccessfully() throws IOException {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("tests.xlsx");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        TestCase tc = new TestCase("TC001", "Test", "Desc");
        tc.setStatus("PASS");
        tc.setTotalDurationMs(100L);
        ExecutionReport report = new ExecutionReport(Collections.singletonList(tc));

        when(excelTestRunner.run(any(InputStream.class))).thenReturn(report);

        ResponseEntity<Map<String, Object>> response = controller.executeTests(file);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals("completed", response.getBody().get("status"));
        Assert.assertEquals(1, response.getBody().get("totalTestCases"));
        Assert.assertEquals(1, response.getBody().get("passed"));
        Assert.assertEquals(0, response.getBody().get("failed"));
        Assert.assertEquals(0, response.getBody().get("skipped"));
    }

    @Test
    public void shouldReturnBadRequestForEmptyFile() {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = controller.executeTests(file);

        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assert.assertEquals("error", response.getBody().get("status"));
        Assert.assertEquals("Uploaded file is empty", response.getBody().get("message"));
    }

    @Test
    public void shouldReturnBadRequestForNonXlsxFile() {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("tests.csv");

        ResponseEntity<Map<String, Object>> response = controller.executeTests(file);

        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assert.assertEquals("error", response.getBody().get("status"));
        Assert.assertEquals("Only .xlsx files are supported", response.getBody().get("message"));
    }

    @Test
    public void shouldReturnBadRequestForNullFilename() {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn(null);

        ResponseEntity<Map<String, Object>> response = controller.executeTests(file);

        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assert.assertEquals("error", response.getBody().get("status"));
    }

    @Test
    public void shouldReturnInternalServerErrorOnException() throws IOException {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("tests.xlsx");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(excelTestRunner.run(any(InputStream.class)))
                .thenThrow(new RuntimeException("Parsing failed"));

        ResponseEntity<Map<String, Object>> response = controller.executeTests(file);

        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Assert.assertEquals("error", response.getBody().get("status"));
        Assert.assertTrue(((String) response.getBody().get("message")).contains("Parsing failed"));
    }

    @Test
    public void shouldHandleXlsxExtensionCaseInsensitive() throws IOException {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("tests.XLSX");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        ExecutionReport report = new ExecutionReport(Collections.emptyList());
        when(excelTestRunner.run(any(InputStream.class))).thenReturn(report);

        ResponseEntity<Map<String, Object>> response = controller.executeTests(file);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void shouldIncludeExecutionTimestampInResponse() throws IOException {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("tests.xlsx");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        ExecutionReport report = new ExecutionReport(Collections.emptyList());
        when(excelTestRunner.run(any(InputStream.class))).thenReturn(report);

        ResponseEntity<Map<String, Object>> response = controller.executeTests(file);

        Assert.assertNotNull(response.getBody().get("executionTimestamp"));
        Assert.assertNotNull(response.getBody().get("totalDurationMs"));
    }
}
