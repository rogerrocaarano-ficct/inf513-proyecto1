package grupo03sc.mailapp.messaging;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * GreenMail on the ports of the test configuration: SMTP 3025 and POP3 3110.
 */
public final class MailTestSupport {

    public static final String SYSTEM = "sistema@mailapp.test";
    public static final String ADMIN = "admin@mailapp.test";

    private MailTestSupport() {
    }

    public static GreenMailExtension greenMail() {
        return new GreenMailExtension(ServerSetupTest.SMTP_POP3)
                .withConfiguration(GreenMailConfiguration.aConfig().withUser(SYSTEM, SYSTEM, "secreto"));
    }

    public static void sendCommand(String from, String subject) throws MessagingException {
        send(message(from, SYSTEM, subject));
    }

    public static MimeMessage message(String from, String to, String subject) throws MessagingException {
        MimeMessage message = new MimeMessage(session());
        message.setFrom(new InternetAddress(from));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject, "UTF-8");
        message.setText("", "UTF-8");
        return message;
    }

    public static void sendWithAttachment(String from, String subject, String fileName, byte[] content)
            throws MessagingException {
        MimeMessage message = message(from, SYSTEM, subject);
        MimeBodyPart text = new MimeBodyPart();
        text.setText("Adjunto", "UTF-8");
        MimeBodyPart file = new MimeBodyPart();
        file.setContent(content, "application/pdf");
        file.setFileName(fileName);
        file.setDisposition(MimeBodyPart.ATTACHMENT);
        message.setContent(new MimeMultipart(text, file));
        send(message);
    }

    public static void send(MimeMessage message) throws MessagingException {
        message.saveChanges();
        Transport.send(message);
    }

    /** Messages that GreenMail received for one recipient. */
    public static List<MimeMessage> receivedBy(GreenMailExtension greenMail, String recipient) {
        return Arrays.stream(greenMail.getReceivedMessages()).filter(message -> {
            try {
                return Arrays.stream(message.getAllRecipients())
                        .anyMatch(address -> ((InternetAddress) address).getAddress().equalsIgnoreCase(recipient));
            } catch (MessagingException e) {
                throw new IllegalStateException(e);
            }
        }).toList();
    }

    public static String body(MimeMessage message) {
        try {
            return message.getContent().toString().strip();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static Session session() {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "localhost");
        properties.put("mail.smtp.port", "3025");
        return Session.getInstance(properties);
    }
}
