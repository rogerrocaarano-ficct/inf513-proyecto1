package grupo03sc.mailapp.audit.application;

import grupo03sc.mailapp.audit.domain.ChangeLogEntry;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.Repository;

/**
 * Insert and read only: change log entries are never modified or deleted.
 */
public interface ChangeLogEntryRepository extends Repository<ChangeLogEntry, UUID> {

    ChangeLogEntry save(ChangeLogEntry entry);

    List<ChangeLogEntry> findByTargetTypeAndTargetIdOrderByChangedAt(String targetType, UUID targetId);

    /** Same order, and inside one change by id (UUID version 7, time-ordered), so the order is stable. */
    List<ChangeLogEntry> findByTargetTypeAndTargetIdOrderByChangedAtAscIdAsc(String targetType, UUID targetId);
}
