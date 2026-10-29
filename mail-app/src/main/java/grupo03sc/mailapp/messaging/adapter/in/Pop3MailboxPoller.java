package grupo03sc.mailapp.messaging.adapter.in;

import grupo03sc.mailapp.messaging.application.CommandDispatcher;
import grupo03sc.mailapp.messaging.application.CommandReply;
import grupo03sc.mailapp.messaging.application.MailProperties;
import grupo03sc.mailapp.messaging.application.OutgoingMail;
import grupo03sc.mailapp.messaging.domain.IncomingMail;
import grupo03sc.mailapp.messaging.domain.IncomingMailFilter;
import grupo03sc.mailapp.messaging.domain.MailAttachment;
import grupo03sc.mailapp.messaging.domain.MailDecision;
import jakarta.mail.Address;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.InternetAddress;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Polls the POP3 mailbox and handles each new command email, one at a time (AD-005, D4).
 */
@Component
public class Pop3MailboxPoller {

    private static final Logger log = LoggerFactory.getLogger(Pop3MailboxPoller.class);

    private final MailProperties properties;
    private final IncomingMailFilter filter;
    private final CommandDispatcher dispatcher;
    private final OutgoingMail outgoingMail;

    Pop3MailboxPoller(MailProperties properties, CommandDispatcher dispatcher, OutgoingMail outgoingMail) {
        this.properties = properties;
        this.filter = new IncomingMailFilter(properties.address());
        this.dispatcher = dispatcher;
        this.outgoingMail = outgoingMail;
    }

    public synchronized void poll() {
        MailProperties.Pop3 pop3 = properties.pop3();
        Properties sessionProperties = new Properties();
        sessionProperties.put("mail.pop3.host", pop3.host());
        sessionProperties.put("mail.pop3.port", String.valueOf(pop3.port()));
        Session session = Session.getInstance(sessionProperties);
        try (Store store = session.getStore("pop3")) {
            store.connect(pop3.host(), pop3.port(), pop3.user(), pop3.password());
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);
            try {
                for (Message message : inbox.getMessages()) {
                    handle(message);
                }
            } finally {
                inbox.close(true);
            }
        } catch (MessagingException e) {
            log.error("Could not poll the mailbox {}@{}:{}", pop3.user(), pop3.host(), pop3.port(), e);
        }
    }

    private void handle(Message message) {
        try {
            IncomingMail mail = read(message);
            MailDecision decision = filter.decide(mail);
            if (decision == MailDecision.LEAVE) {
                return;
            }
            if (decision == MailDecision.PROCESS && !dispatcher.alreadyProcessed(mail.messageId())) {
                CommandReply reply = dispatcher.dispatch(mail);
                outgoingMail.send(mail.from(), "Re: " + mail.subject(), reply.body());
            }
            message.setFlag(Flags.Flag.DELETED, true);
        } catch (MessagingException | IOException | RuntimeException e) {
            log.error("Could not handle a message from the mailbox", e);
        }
    }

    private static IncomingMail read(Message message) throws MessagingException, IOException {
        String from = firstAddress(message.getFrom());
        List<String> recipients = new ArrayList<>();
        addAddresses(recipients, message.getRecipients(Message.RecipientType.TO));
        addAddresses(recipients, message.getRecipients(Message.RecipientType.CC));
        String subject = message.getSubject() == null ? "" : message.getSubject();
        List<MailAttachment> attachments = new ArrayList<>();
        collectAttachments(message, attachments);
        return new IncomingMail(messageId(message, from, subject), from, recipients,
                header(message, "Auto-Submitted"), subject, attachments);
    }

    private static void collectAttachments(Part part, List<MailAttachment> attachments)
            throws MessagingException, IOException {
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                collectAttachments(multipart.getBodyPart(i), attachments);
            }
        } else if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) || part.getFileName() != null) {
            try (var input = part.getInputStream()) {
                attachments.add(new MailAttachment(part.getFileName(), baseType(part.getContentType()), input.readAllBytes()));
            }
        }
    }

    /** Uses the Message-ID header, or a hash of the message when the header is missing. */
    private static String messageId(Message message, String from, String subject) throws MessagingException {
        String id = header(message, "Message-ID");
        if (id != null && !id.isBlank()) {
            return id.strip();
        }
        String sent = message.getSentDate() == null ? "" : message.getSentDate().toInstant().toString();
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest((from + "|" + sent + "|" + subject).getBytes(StandardCharsets.UTF_8));
            return "sha256:" + HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String header(Message message, String name) throws MessagingException {
        String[] values = message.getHeader(name);
        return values == null || values.length == 0 ? null : values[0];
    }

    private static String firstAddress(Address[] addresses) {
        return addresses == null || addresses.length == 0 ? "" : email(addresses[0]);
    }

    private static void addAddresses(List<String> target, Address[] addresses) {
        if (addresses != null) {
            for (Address address : addresses) {
                target.add(email(address));
            }
        }
    }

    private static String email(Address address) {
        return address instanceof InternetAddress internet ? internet.getAddress() : address.toString();
    }

    private static String baseType(String contentType) {
        return contentType == null ? null : contentType.split(";")[0].strip();
    }
}
