package grupo03sc.mailapp.audit.application;

import java.time.Instant;
import java.util.UUID;

/**
 * An entry of the change log of a record, as {@link ChangeLog#history} returns it. The entries of one
 * {@link ChangeLog#record} call share {@code changedAt}.
 */
public record LoggedChange(Instant changedAt, UUID actorId, String field, String oldValue, String newValue) {
}
