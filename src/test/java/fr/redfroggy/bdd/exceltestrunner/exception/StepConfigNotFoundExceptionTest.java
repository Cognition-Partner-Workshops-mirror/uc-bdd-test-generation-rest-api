package fr.redfroggy.bdd.exceltestrunner.exception;

import org.junit.Assert;
import org.junit.Test;

public class StepConfigNotFoundExceptionTest {

    @Test
    public void shouldCreateWithMessage() {
        StepConfigNotFoundException ex = new StepConfigNotFoundException("not found");
        Assert.assertEquals("not found", ex.getMessage());
    }

    @Test
    public void shouldCreateWithMessageAndCause() {
        Throwable cause = new RuntimeException("root cause");
        StepConfigNotFoundException ex = new StepConfigNotFoundException("not found", cause);
        Assert.assertEquals("not found", ex.getMessage());
        Assert.assertEquals(cause, ex.getCause());
    }
}
