package bo.org.tecnoweb.cpms.mail.infrastructure.persistence;

import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmailState;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity of a {@link bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail}, table {@code received_email}.
 *
 * <p>Accessors are package-private: only {@link JpaReceivedEmailRepository} uses this class.
 */
@Entity
@Table(name = "received_email")
class ReceivedEmailEntity {

    @Id
    private UUID id;

    @Column(name = "mailbox_id", nullable = false, unique = true)
    private String mailboxId;

    @Column(name = "from_address")
    private String fromAddress;

    private String subject;

    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReceivedEmailState state;

    /** Set by the database on insert; orders the pending emails. */
    @Column(name = "received_at", insertable = false, updatable = false)
    private Instant receivedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "received_email_id", nullable = false)
    @OrderColumn(name = "position")
    private List<ReceivedEmailAttachmentEntity> attachments = new ArrayList<>();

    /**
     * Constructor required by JPA.
     */
    protected ReceivedEmailEntity() {
    }

    /**
     * Creates an entity for a new email.
     *
     * @param id identifier of the domain email
     */
    ReceivedEmailEntity(UUID id) {
        this.id = id;
    }

    UUID getId() {
        return id;
    }

    String getMailboxId() {
        return mailboxId;
    }

    void setMailboxId(String mailboxId) {
        this.mailboxId = mailboxId;
    }

    String getFromAddress() {
        return fromAddress;
    }

    void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    String getSubject() {
        return subject;
    }

    void setSubject(String subject) {
        this.subject = subject;
    }

    String getBody() {
        return body;
    }

    void setBody(String body) {
        this.body = body;
    }

    ReceivedEmailState getState() {
        return state;
    }

    void setState(ReceivedEmailState state) {
        this.state = state;
    }

    List<ReceivedEmailAttachmentEntity> getAttachments() {
        return attachments;
    }
}
