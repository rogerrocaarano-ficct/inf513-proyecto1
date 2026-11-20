package bo.org.tecnoweb.cpms.mail.infrastructure;

import bo.org.tecnoweb.cpms.mail.application.EmailSender;
import bo.org.tecnoweb.cpms.mail.domain.File;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * SMTP adapter of {@link EmailSender}, built on Spring's {@link JavaMailSender}.
 *
 * <p>Every email is sent in UTF-8 from the system address ({@link MailProperties#address()}).
 */
@Component
class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;
    private final MailProperties properties;

    /**
     * Creates the adapter.
     *
     * @param mailSender Spring mail sender configured with {@code spring.mail.*}
     * @param properties mail settings, for the system address
     */
    SmtpEmailSender(JavaMailSender mailSender, MailProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if the message cannot be built
     * @throws org.springframework.mail.MailException if the SMTP server rejects or cannot receive it
     */
    @Override
    public void send(String to, String subject, String body, List<File> attachments) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            boolean multipart = attachments != null && !attachments.isEmpty();
            MimeMessageHelper helper = new MimeMessageHelper(message, multipart, StandardCharsets.UTF_8.name());
            helper.setFrom(properties.address());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            if (multipart) {
                for (File file : attachments) {
                    helper.addAttachment(file.getName(), new ByteArrayResource(file.getData()));
                }
            }
        } catch (MessagingException e) {
            throw new IllegalStateException("Could not build the email to " + to, e);
        }
        mailSender.send(message);
    }
}
