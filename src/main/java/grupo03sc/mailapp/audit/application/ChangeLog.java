package grupo03sc.mailapp.audit.application;

import java.util.List;
import java.util.UUID;

/**
 * Interface provided by Audit: records changes in the current transaction (AD-008).
 */
public interface ChangeLog {

    /** Records the changes in the current transaction; all of them share the same date and time. */
    void record(UUID actorId, String targetType, UUID targetId, List<FieldChange> changes);

    /** The entries of a record in the order they were made, oldest first (UC-004). */
    List<LoggedChange> history(String targetType, UUID targetId);
}
