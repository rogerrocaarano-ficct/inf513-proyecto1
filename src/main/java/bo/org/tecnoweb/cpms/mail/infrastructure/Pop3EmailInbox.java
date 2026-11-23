package bo.org.tecnoweb.cpms.mail.infrastructure;

import bo.org.tecnoweb.cpms.mail.application.EmailInbox;
import bo.org.tecnoweb.cpms.mail.domain.Attachment;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import jakarta.mail.Address;
import jakarta.mail.FetchProfile;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.UIDFolder;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeUtility;
import jakarta.mail.internet.ParseException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import org.eclipse.angus.mail.pop3.POP3Folder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * POP3 adapter of {@link EmailInbox}, built on Jakarta Mail.
 *
 * <p>Messages are identified by their UIDL, never by their sequence number, which changes between
 * sessions. Automatic emails (bounces, auto-replies, bulk mail and mail from the system itself)
 * are never returned: they are deleted in the same session so they cannot fill the batch.
 */
@Component
class Pop3EmailInbox implements EmailInbox {

    private static final Logger log = LoggerFactory.getLogger(Pop3EmailInbox.class);
    private static final Set<String> BULK_PRECEDENCE = Set.of("bulk", "junk", "list");
    private static final String TIMEOUT_MS = "10000";
    private static final String UNKNOWN_CONTENT_TYPE = "application/octet-stream";

    private final MailProperties properties;
    private final Session session;

    /**
     * Creates the adapter.
     *
     * @param properties mail settings: POP3 server and system address
     */
    Pop3EmailInbox(MailProperties properties) {
        this.properties = properties;
        Properties settings = new Properties();
        settings.put("mail.pop3.host", nullToEmpty(properties.pop3().host()));
        settings.put("mail.pop3.port", String.valueOf(properties.pop3().port()));
        settings.put("mail.pop3.connectiontimeout", TIMEOUT_MS);
        settings.put("mail.pop3.timeout", TIMEOUT_MS);
        this.session = Session.getInstance(settings);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Automatic emails found while reading are deleted from the server and do not count toward
     * {@code maxMessages}.
     */
    @Override
    public List<ReceivedEmail> poll(int maxMessages) {
        List<ReceivedEmail> emails = new ArrayList<>();
        withInbox(folder -> {
            for (Message message : folder.getMessages()) {
                if (emails.size() >= maxMessages) {
                    break;
                }
                String uid = folder.getUID(message);
                if (isAutomatic(message)) {
                    log.info("Ignoring automatic email {}", uid);
                    message.setFlag(Flags.Flag.DELETED, true);
                    continue;
                }
                emails.add(toEmail(message, uid));
            }
        });
        return emails;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(String mailboxId) {
        withInbox(folder -> {
            for (Message message : folder.getMessages()) {
                if (mailboxId.equals(folder.getUID(message))) {
                    message.setFlag(Flags.Flag.DELETED, true);
                }
            }
        });
    }

    /**
     * Work done on the open inbox folder.
     */
    @FunctionalInterface
    private interface FolderWork {

        /**
         * Runs the work.
         *
         * @param folder the inbox, open for reading and writing, with the UIDLs already fetched
         * @throws MessagingException if the server fails
         */
        void run(POP3Folder folder) throws MessagingException;
    }

    /**
     * Opens the inbox, runs the work and closes it. Messages flagged DELETED are removed when the
     * folder closes (POP3 QUIT); the others stay on the server.
     *
     * @param work what to do with the open folder
     * @throws IllegalStateException if the server cannot be reached or fails
     */
    private void withInbox(FolderWork work) {
        Store store = null;
        POP3Folder folder = null;
        try {
            store = session.getStore("pop3");
            store.connect(properties.pop3().host(), properties.pop3().port(),
                    properties.pop3().username(), properties.pop3().password());
            folder = (POP3Folder) store.getFolder("INBOX");
            folder.open(Folder.READ_WRITE);
            FetchProfile uids = new FetchProfile();
            uids.add(UIDFolder.FetchProfileItem.UID);
            folder.fetch(folder.getMessages(), uids);
            work.run(folder);
        } catch (MessagingException e) {
            throw new IllegalStateException("POP3 mailbox error: " + e.getMessage(), e);
        } finally {
            close(folder, store);
        }
    }

    private static void close(Folder folder, Store store) {
        try {
            if (folder != null && folder.isOpen()) {
                folder.close(true);
            }
        } catch (MessagingException e) {
            log.warn("Could not close the POP3 folder", e);
        }
        try {
            if (store != null) {
                store.close();
            }
        } catch (MessagingException e) {
            log.warn("Could not close the POP3 store", e);
        }
    }

    /**
     * Tells whether a message was generated automatically and must be ignored.
     *
     * @param message the message to check
     * @return {@code true} for auto-replies ({@code Auto-Submitted} other than {@code no}), bulk mail
     *         ({@code Precedence: bulk|junk|list}), bounces (empty {@code Return-Path}, no sender or
     *         {@code MAILER-DAEMON}) and mail sent from the system address
     * @throws MessagingException if the headers cannot be read
     */
    private boolean isAutomatic(Message message) throws MessagingException {
        String autoSubmitted = firstHeader(message, "Auto-Submitted");
        if (autoSubmitted != null && !autoSubmitted.strip().toLowerCase(Locale.ROOT).startsWith("no")) {
            return true;
        }
        String precedence = firstHeader(message, "Precedence");
        if (precedence != null && BULK_PRECEDENCE.contains(precedence.strip().toLowerCase(Locale.ROOT))) {
            return true;
        }
        String returnPath = firstHeader(message, "Return-Path");
        if (returnPath != null && returnPath.strip().equals("<>")) {
            return true;
        }
        String from = senderAddress(message);
        if (from == null || from.isBlank()) {
            return true;
        }
        String localPart = from.contains("@") ? from.substring(0, from.indexOf('@')) : from;
        if (localPart.equalsIgnoreCase("mailer-daemon")) {
            return true;
        }
        return properties.address() != null && from.equalsIgnoreCase(properties.address().strip());
    }

    private static String firstHeader(Message message, String name) throws MessagingException {
        String[] values = message.getHeader(name);
        return values == null || values.length == 0 ? null : values[0];
    }

    private static String senderAddress(Message message) throws MessagingException {
        Address[] from = message.getFrom();
        if (from == null || from.length == 0) {
            return null;
        }
        return from[0] instanceof InternetAddress internet ? internet.getAddress() : from[0].toString();
    }

    /**
     * Converts a message into a new email.
     *
     * <p>A message that cannot be read completely keeps what could be read; its email is later
     * discarded and answered, so it never blocks the mailbox.
     *
     * @param message the message read from the server
     * @param uid     its UIDL
     * @return a new email in state {@link bo.org.tecnoweb.cpms.mail.domain.ReceivedEmailState#FETCHED}
     */
    private static ReceivedEmail toEmail(Message message, String uid) {
        ReceivedEmail email = ReceivedEmail.builder().withMailboxId(uid);
        try {
            email.fromSender(senderAddress(message));
            email.withSubject(message.getSubject());
            Content content = new Content();
            collect(message, content);
            email.withBody(content.body).withAttachments(content.attachments);
        } catch (MessagingException | IOException e) {
            log.warn("Message {} could not be read completely", uid, e);
        }
        return email;
    }

    /**
     * Body and attachments collected from the parts of a message.
     */
    private static final class Content {
        String body;
        final List<Attachment> attachments = new ArrayList<>();
    }

    /**
     * Walks a part and its subparts. The first plain text part becomes the body; parts with a file
     * name or an attachment disposition become attachments.
     *
     * @param part    the part to read
     * @param content where the body and the attachments are collected
     * @throws MessagingException if the part cannot be read
     * @throws IOException        if the content of the part cannot be read
     */
    private static void collect(Part part, Content content) throws MessagingException, IOException {
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                collect(multipart.getBodyPart(i), content);
            }
            return;
        }
        String fileName = part.getFileName();
        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) || fileName != null) {
            String name = fileName == null || fileName.isBlank() ? "adjunto" : MimeUtility.decodeText(fileName);
            byte[] data = part.getInputStream().readAllBytes();
            content.attachments.add(new Attachment.Content(name, baseType(part.getContentType()), data));
            return;
        }
        if (content.body == null && part.isMimeType("text/plain")) {
            content.body = String.valueOf(part.getContent());
        }
    }

    /**
     * Reduces the content type of a part to its base type, which file storage keeps (issue #9).
     *
     * @param contentType the {@code Content-Type} header of the part
     * @return the base type in lower case, without parameters, or {@code application/octet-stream}
     *         when the header is missing or cannot be parsed
     */
    static String baseType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return UNKNOWN_CONTENT_TYPE;
        }
        try {
            return new ContentType(contentType).getBaseType().toLowerCase(Locale.ROOT);
        } catch (ParseException e) {
            return UNKNOWN_CONTENT_TYPE;
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
