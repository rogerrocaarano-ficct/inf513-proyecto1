package grupo03sc.mailapp.messaging.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

/**
 * An email already processed, kept so that a command never runs twice (D4).
 */
@Entity
@Table(name = "processed_mail_messages")
public class ProcessedMailMessage {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "message_id", nullable = false)
    private String messageId;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    protected ProcessedMailMessage() {
    }

    public ProcessedMailMessage(String messageId, Instant processedAt) {
        this.messageId = messageId;
        this.processedAt = processedAt;
    }

    public String getMessageId() { return messageId; }
}
