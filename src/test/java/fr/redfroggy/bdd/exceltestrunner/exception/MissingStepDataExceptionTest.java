package fr.redfroggy.bdd.exceltestrunner.exception;

import org.junit.Assert;
import org.junit.Test;

public class MissingStepDataExceptionTest {

    @Test
    public void shouldCreateWithMessage() {
        MissingStepDataException ex = new MissingStepDataException("missing field");
        Assert.assertEquals("missing field", ex.getMessage());
    }

    @Test
    public void shouldCreateWithMessageAndCause() {
        Throwable cause = new RuntimeException("root cause");
        MissingStepDataException ex = new MissingStepDataException("missing field", cause);
        Assert.assertEquals("missing field", ex.getMessage());
        Assert.assertEquals(cause, ex.getCause());
    }
}
