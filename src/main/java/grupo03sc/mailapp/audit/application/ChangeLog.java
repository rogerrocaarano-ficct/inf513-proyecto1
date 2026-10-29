package grupo03sc.mailapp.audit.application;

import java.util.List;
import java.util.UUID;

/**
 * Interface provided by Audit: records changes in the current transaction (AD-008).
 */
public interface ChangeLog {

    void record(UUID actorId, String targetType, UUID targetId, List<FieldChange> changes);
}
