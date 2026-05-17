package fr.redfroggy.bdd.exceltestrunner.exception;

import org.junit.Assert;
import org.junit.Test;

public class InvalidExcelTemplateExceptionTest {

    @Test
    public void shouldCreateWithMessage() {
        InvalidExcelTemplateException ex = new InvalidExcelTemplateException("bad template");
        Assert.assertEquals("bad template", ex.getMessage());
    }

    @Test
    public void shouldCreateWithMessageAndCause() {
        Throwable cause = new RuntimeException("root cause");
        InvalidExcelTemplateException ex = new InvalidExcelTemplateException("bad template", cause);
        Assert.assertEquals("bad template", ex.getMessage());
        Assert.assertEquals(cause, ex.getCause());
    }
}
