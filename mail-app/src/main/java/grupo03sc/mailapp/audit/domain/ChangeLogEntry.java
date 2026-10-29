package grupo03sc.mailapp.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Immutable
@Table(name = "change_log_entries")
public class ChangeLogEntry {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Column(name = "target_type", nullable = false)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(nullable = false)
    private String field;

    @Column(name = "old_value")
    private String oldValue;

    @Column(name = "new_value")
    private String newValue;

    protected ChangeLogEntry() {
    }

    public ChangeLogEntry(Instant changedAt, UUID actorId, String targetType, UUID targetId,
            String field, String oldValue, String newValue) {
        this.changedAt = changedAt;
        this.actorId = actorId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.field = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public UUID getId() { return id; }
    public Instant getChangedAt() { return changedAt; }
    public UUID getActorId() { return actorId; }
    public String getTargetType() { return targetType; }
    public UUID getTargetId() { return targetId; }
    public String getField() { return field; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
}
