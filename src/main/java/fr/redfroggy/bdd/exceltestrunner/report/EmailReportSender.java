package fr.redfroggy.bdd.exceltestrunner.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * Sends the HTML execution report via email using Spring JavaMailSender
 * to the configured recipient.
 *
 * @author Ashish,Raut
 */
@Component
public class EmailReportSender {

    private static final Logger logger = LoggerFactory.getLogger(EmailReportSender.class);

    // Spring JavaMailSender for sending emails
    private final JavaMailSender mailSender;

    // Recipient email address configured via application properties
    @Value("${exceltestrunner.email.to:test-reports@example.com}")
    private String recipientEmail;

    // Sender email address configured via application properties
    @Value("${exceltestrunner.email.from:noreply@exceltestrunner.com}")
    private String fromEmail;

    /**
     * Constructs an EmailReportSender with the required JavaMailSender dependency.
     *
     * @param mailSender the Spring JavaMailSender for sending emails
     */
    public EmailReportSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends the HTML execution report via email to the configured recipient.
     *
     * @param htmlReport the HTML report content to send as the email body
     * @param subject    the email subject line
     */
    public void sendReport(String htmlReport, String subject) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(recipientEmail);
            helper.setFrom(fromEmail);
            helper.setSubject(subject);
            // Set the HTML content as the email body
            helper.setText(htmlReport, true);

            mailSender.send(message);
            logger.info("Execution report email sent successfully to {}", recipientEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send execution report email to {}: {}", recipientEmail, e.getMessage());
            throw new RuntimeException("Failed to send execution report email", e);
        }
    }

    /**
     * Sets the recipient email address (useful for testing).
     *
     * @param recipientEmail the recipient email address
     */
    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    /**
     * Sets the sender email address (useful for testing).
     *
     * @param fromEmail the sender email address
     */
    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }
}
