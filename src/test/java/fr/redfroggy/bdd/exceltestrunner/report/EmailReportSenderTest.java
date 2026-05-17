package fr.redfroggy.bdd.exceltestrunner.report;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EmailReportSenderTest {

    private JavaMailSender mailSender;
    private EmailReportSender emailReportSender;

    @Before
    public void setUp() {
        mailSender = Mockito.mock(JavaMailSender.class);
        emailReportSender = new EmailReportSender(mailSender);
        emailReportSender.setRecipientEmail("test@example.com");
        emailReportSender.setFromEmail("noreply@test.com");
    }

    @Test
    public void shouldSendReportSuccessfully() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailReportSender.sendReport("<html>Report</html>", "Test Report");

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionWhenSendFails() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        emailReportSender.sendReport("<html>Report</html>", "Test Report");
    }

    @Test
    public void shouldSetRecipientEmail() {
        emailReportSender.setRecipientEmail("new@example.com");
        // No assertion needed - verifying setter doesn't throw
    }

    @Test
    public void shouldSetFromEmail() {
        emailReportSender.setFromEmail("new-from@example.com");
        // No assertion needed - verifying setter doesn't throw
    }
}
