package grupo03sc.mailapp.messaging.adapter.out;

import grupo03sc.mailapp.messaging.application.MailProperties;
import grupo03sc.mailapp.messaging.application.OutgoingMail;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
class SmtpOutgoingMail implements OutgoingMail {

    private final JavaMailSender mailSender;
    private final MailProperties properties;

    SmtpOutgoingMail(JavaMailSender mailSender, MailProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public void send(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(properties.address());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            message.setHeader("Auto-Submitted", "auto-replied");
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new MailSendException("Could not build the message to " + to, e);
        }
    }
}
