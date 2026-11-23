package bo.org.tecnoweb.cpms.mail.infrastructure;

import bo.org.tecnoweb.cpms.mail.application.EmailSender;
import bo.org.tecnoweb.cpms.storage.application.StoredFileService;
import bo.org.tecnoweb.cpms.storage.domain.StoredFile;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * SMTP adapter of {@link EmailSender}, built on Spring's {@link JavaMailSender}.
 *
 * <p>Every email is sent in UTF-8 from the system address ({@link MailProperties#address()}). The
 * attachments are read from file storage with their original name and media type (issue #9).
 */
@Component
class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;
    private final MailProperties properties;
    private final StoredFileService storedFiles;

    /**
     * Creates the adapter.
     *
     * @param mailSender  Spring mail sender configured with {@code spring.mail.*}
     * @param properties  mail settings, for the system address
     * @param storedFiles file storage, where the files to attach are kept
     */
    SmtpEmailSender(JavaMailSender mailSender, MailProperties properties, StoredFileService storedFiles) {
        this.mailSender = mailSender;
        this.properties = properties;
        this.storedFiles = storedFiles;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Every attachment is read before the message is built, so nothing is sent if one cannot be
     * read.
     *
     * @throws bo.org.tecnoweb.cpms.storage.domain.StoredFileNotFoundException if an attachment does
     *                                                                         not exist
     * @throws IllegalStateException if the message cannot be built
     * @throws org.springframework.mail.MailException if the SMTP server rejects or cannot receive it
     */
    @Override
    public void send(String to, String subject, String body, List<UUID> attachmentIds) {
        List<OutgoingFile> files = attachmentIds == null ? List.of() : attachmentIds.stream()
                .map(id -> new OutgoingFile(storedFiles.get(id), storedFiles.readContent(id)))
                .toList();
        MimeMessage message = mailSender.createMimeMessage();
        try {
            boolean multipart = !files.isEmpty();
            MimeMessageHelper helper = new MimeMessageHelper(message, multipart, StandardCharsets.UTF_8.name());
            helper.setFrom(properties.address());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            for (OutgoingFile file : files) {
                helper.addAttachment(file.metadata().getOriginalName(), new ByteArrayResource(file.content()),
                        file.metadata().getContentType());
            }
        } catch (MessagingException e) {
            throw new IllegalStateException("Could not build the email to " + to, e);
        }
        mailSender.send(message);
    }

    /**
     * A stored file read to be attached.
     *
     * @param metadata its name and media type
     * @param content  its bytes
     */
    private record OutgoingFile(StoredFile metadata, byte[] content) {
    }
}
