package fr.redfroggy.bdd.exceltestrunner;

import fr.redfroggy.bdd.exceltestrunner.executor.TestCaseExecutor;
import fr.redfroggy.bdd.exceltestrunner.model.TestCase;
import fr.redfroggy.bdd.exceltestrunner.parser.ExcelParser;
import fr.redfroggy.bdd.exceltestrunner.report.EmailReportSender;
import fr.redfroggy.bdd.exceltestrunner.report.ExecutionReport;
import fr.redfroggy.bdd.exceltestrunner.report.ReportGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExcelTestRunnerTest {

    private ExcelParser excelParser;
    private TestCaseExecutor testCaseExecutor;
    private ReportGenerator reportGenerator;
    private EmailReportSender emailReportSender;
    private ExcelTestRunner excelTestRunner;

    @Before
    public void setUp() {
        excelParser = Mockito.mock(ExcelParser.class);
        testCaseExecutor = Mockito.mock(TestCaseExecutor.class);
        reportGenerator = Mockito.mock(ReportGenerator.class);
        emailReportSender = Mockito.mock(EmailReportSender.class);
        excelTestRunner = new ExcelTestRunner(excelParser, testCaseExecutor, reportGenerator, emailReportSender);
    }

    @Test
    public void shouldRunFullPipeline() throws IOException {
        TestCase tc = new TestCase("TC001", "Test", "Desc");
        tc.setStatus("PASS");
        tc.setTotalDurationMs(100L);
        List<TestCase> testCases = Collections.singletonList(tc);

        when(excelParser.parse(any(InputStream.class))).thenReturn(testCases);
        doNothing().when(testCaseExecutor).executeAll(anyList());
        when(reportGenerator.generateHtml(any(ExecutionReport.class))).thenReturn("<html>Report</html>");
        doNothing().when(emailReportSender).sendReport(anyString(), anyString());

        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        ExecutionReport report = excelTestRunner.run(inputStream);

        Assert.assertNotNull(report);
        Assert.assertEquals(1, report.getTotalCount());
        Assert.assertEquals(1, report.getPassCount());

        verify(excelParser, times(1)).parse(any(InputStream.class));
        verify(testCaseExecutor, times(1)).executeAll(anyList());
        verify(reportGenerator, times(1)).generateHtml(any(ExecutionReport.class));
        verify(emailReportSender, times(1)).sendReport(anyString(), anyString());
    }

    @Test
    public void shouldHandleEmailFailureGracefully() throws IOException {
        TestCase tc = new TestCase("TC001", "Test", "Desc");
        tc.setStatus("FAIL");
        tc.setTotalDurationMs(50L);

        when(excelParser.parse(any(InputStream.class))).thenReturn(Collections.singletonList(tc));
        doNothing().when(testCaseExecutor).executeAll(anyList());
        when(reportGenerator.generateHtml(any(ExecutionReport.class))).thenReturn("<html>Report</html>");
        doThrow(new RuntimeException("SMTP error")).when(emailReportSender)
                .sendReport(anyString(), anyString());

        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        ExecutionReport report = excelTestRunner.run(inputStream);

        // Pipeline should still complete even if email fails
        Assert.assertNotNull(report);
        Assert.assertEquals(1, report.getTotalCount());
        Assert.assertEquals(1, report.getFailCount());
    }

    @Test
    public void shouldHandleMultipleTestCases() throws IOException {
        TestCase tc1 = new TestCase("TC001", "Test 1", "Desc 1");
        tc1.setStatus("PASS");
        tc1.setTotalDurationMs(100L);

        TestCase tc2 = new TestCase("TC002", "Test 2", "Desc 2");
        tc2.setStatus("FAIL");
        tc2.setTotalDurationMs(200L);

        when(excelParser.parse(any(InputStream.class))).thenReturn(Arrays.asList(tc1, tc2));
        doNothing().when(testCaseExecutor).executeAll(anyList());
        when(reportGenerator.generateHtml(any(ExecutionReport.class))).thenReturn("<html>Report</html>");
        doNothing().when(emailReportSender).sendReport(anyString(), anyString());

        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        ExecutionReport report = excelTestRunner.run(inputStream);

        Assert.assertEquals(2, report.getTotalCount());
        Assert.assertEquals(1, report.getPassCount());
        Assert.assertEquals(1, report.getFailCount());
    }

    @Test
    public void shouldHandleEmptyTestCaseList() throws IOException {
        when(excelParser.parse(any(InputStream.class))).thenReturn(Collections.emptyList());
        doNothing().when(testCaseExecutor).executeAll(anyList());
        when(reportGenerator.generateHtml(any(ExecutionReport.class))).thenReturn("<html>Report</html>");
        doNothing().when(emailReportSender).sendReport(anyString(), anyString());

        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        ExecutionReport report = excelTestRunner.run(inputStream);

        Assert.assertNotNull(report);
        Assert.assertEquals(0, report.getTotalCount());
    }

    @Test(expected = IOException.class)
    public void shouldPropagateParsingException() throws IOException {
        when(excelParser.parse(any(InputStream.class))).thenThrow(new IOException("Parse error"));

        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        excelTestRunner.run(inputStream);
    }
}
