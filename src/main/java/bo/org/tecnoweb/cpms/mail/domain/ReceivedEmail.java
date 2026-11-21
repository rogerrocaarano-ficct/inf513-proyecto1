package bo.org.tecnoweb.cpms.mail.domain;

import bo.org.tecnoweb.cpms.common.AggregateRoot;
import bo.org.tecnoweb.cpms.common.DomainEvent;
import bo.org.tecnoweb.cpms.common.DomainValidationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Email read from the mailbox and stored to process the command in its subject.
 *
 * <p>The subject has the form {@code command[arg1; arg2; ...]} (NFR-003). The command name is the
 * word right before {@code [}; any text before it, such as {@code "Re:"}, is ignored. The subject
 * must end with {@code ]}.
 *
 * <p>Built with {@link #builder()} for a new email or {@link #rehydrate()} for one read from
 * storage, and completed with the {@code with*} methods. Its lifecycle is described in
 * {@link ReceivedEmailState}.
 */
public class ReceivedEmail implements AggregateRoot {

    /** Command name right before '[', arguments up to the final ']'. Any text before the name is ignored. */
    private static final Pattern COMMAND = Pattern.compile("([A-Za-z0-9_.]+)\\s*\\[(.*)\\]\\s*$");

    /** Field name used in the validation error when the subject does not have the command form. */
    public static final String SUBJECT_FIELD = "asunto";

    /** User-facing explanation of the subject syntax. */
    public static final String SYNTAX_HELP = "El asunto debe tener la forma comando[arg1; arg2; …].";

    private UUID id;
    private String from;
    private String subject;
    private String mailboxId;
    private String body;
    private ReceivedEmailState state;
    private List<File> attachments = List.of();
    private final List<DomainEvent> events = new ArrayList<>();

    private ReceivedEmail() {
    }

    /**
     * Starts a newly received email.
     *
     * @return a new email with a random identifier, in state {@link ReceivedEmailState#RECEIVED}
     */
    public static ReceivedEmail builder() {
        ReceivedEmail email = new ReceivedEmail();
        email.id = UUID.randomUUID();
        email.state = ReceivedEmailState.RECEIVED;
        return email;
    }

    /**
     * Starts an email that already exists, read from storage.
     *
     * @return an empty email whose identity and state must be set with {@link #withId(UUID)} and
     *         {@link #withState(ReceivedEmailState)}
     */
    public static ReceivedEmail rehydrate() {
        return new ReceivedEmail();
    }

    /**
     * Sets the identity.
     *
     * @param id the identifier
     * @return this email
     * @throws NullPointerException if {@code id} is {@code null}
     */
    public ReceivedEmail withId(UUID id) {
        this.id = Objects.requireNonNull(id);
        return this;
    }

    /**
     * Sets the sender.
     *
     * @param from the sender's email address
     * @return this email
     */
    public ReceivedEmail fromSender(String from) {
        this.from = from;
        return this;
    }

    /**
     * Sets the subject, which holds the command.
     *
     * @param subject the decoded subject
     * @return this email
     */
    public ReceivedEmail withSubject(String subject) {
        this.subject = subject;
        return this;
    }

    /**
     * Sets the identifier of the message on the mail server.
     *
     * @param mailboxId the POP3 UIDL of the message, unique and stable between sessions
     * @return this email
     */
    public ReceivedEmail withMailboxId(String mailboxId) {
        this.mailboxId = mailboxId;
        return this;
    }

    /**
     * Sets the text body.
     *
     * @param body the first plain text part of the message
     * @return this email
     */
    public ReceivedEmail withBody(String body) {
        this.body = body;
        return this;
    }

    /**
     * Sets the attachments.
     *
     * @param attachments the attachments in message order; {@code null} means none
     * @return this email
     */
    public ReceivedEmail withAttachments(List<File> attachments) {
        this.attachments = attachments == null ? List.of() : List.copyOf(attachments);
        return this;
    }

    /**
     * Sets the state without checking the transition. Used to rebuild a stored email and to mark it
     * as {@link ReceivedEmailState#VALIDATED}.
     *
     * @param state the new state
     * @return this email
     * @throws NullPointerException if {@code state} is {@code null}
     */
    public ReceivedEmail withState(ReceivedEmailState state) {
        this.state = Objects.requireNonNull(state);
        return this;
    }

    /**
     * Returns the command name written in the subject.
     *
     * @return the command name in lower case, e.g. {@code usuarios.listar} for {@code RE: Usuarios.Listar[]}
     * @throws DomainValidationException if the subject does not have the form {@code command[...]}
     */
    public String parseAction() {
        return matchSubject().group(1).toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the arguments written in the subject.
     *
     * <p>Arguments are separated by {@code ;}. Each one keeps its case and inner spaces and loses its
     * leading and trailing spaces. {@code command[]} has no arguments; {@code command[a;;b]} has an
     * empty second argument.
     *
     * @return the arguments in subject order; empty if there are none
     * @throws DomainValidationException if the subject does not have the form {@code command[...]}
     */
    public List<String> parseArgs() {
        String inside = matchSubject().group(2);
        if (inside.isBlank()) {
            return List.of();
        }
        return Arrays.stream(inside.split(";", -1)).map(String::strip).toList();
    }

    private Matcher matchSubject() {
        Matcher matcher = COMMAND.matcher(subject == null ? "" : subject.strip());
        if (!matcher.find()) {
            throw new DomainValidationException(Map.of(SUBJECT_FIELD, SYNTAX_HELP));
        }
        return matcher;
    }

    /**
     * Records that the use case was executed. Calling it on an email already
     * {@link ReceivedEmailState#PROCESSED} has no effect.
     *
     * @throws IllegalStateException if the email is not {@link ReceivedEmailState#VALIDATED} or
     *                               {@link ReceivedEmailState#PROCESSED}
     */
    public void markAsProcessed() {
        if (state == ReceivedEmailState.PROCESSED) {
            return;
        }
        if (state != ReceivedEmailState.VALIDATED) {
            throw new IllegalStateException("Cannot mark as processed an email in state " + state);
        }
        state = ReceivedEmailState.PROCESSED;
    }

    /**
     * Records that the use case will never be called.
     *
     * @throws IllegalStateException if the email is already in a final state
     */
    public void markAsDiscarded() {
        if (state == null || state.isFinal()) {
            throw new IllegalStateException("Cannot discard an email in state " + state);
        }
        state = ReceivedEmailState.DISCARDED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DomainEvent> pullEvents() {
        List<DomainEvent> pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }

    /**
     * Returns the sender.
     *
     * @return the sender's email address
     */
    public String getFrom() {
        return from;
    }

    /**
     * Returns the subject.
     *
     * @return the decoded subject, as received
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Returns the identifier of the message on the mail server.
     *
     * @return the POP3 UIDL of the message
     */
    public String getMailboxId() {
        return mailboxId;
    }

    /**
     * Returns the text body.
     *
     * @return the first plain text part, or {@code null} if the message has none
     */
    public String getBody() {
        return body;
    }

    /**
     * Returns the attachments.
     *
     * @return an unmodifiable list of the attachments in message order; empty if there are none
     */
    public List<File> getAttachments() {
        return attachments;
    }

    /**
     * Returns the lifecycle state.
     *
     * @return the current state
     */
    public ReceivedEmailState getState() {
        return state;
    }
}
