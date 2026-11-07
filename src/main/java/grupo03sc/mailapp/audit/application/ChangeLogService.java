package grupo03sc.mailapp.audit.application;

import grupo03sc.mailapp.audit.domain.ChangeLogEntry;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
class ChangeLogService implements ChangeLog {

    private final ChangeLogEntryRepository repository;
    private final Clock clock;

    ChangeLogService(ChangeLogEntryRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(UUID actorId, String targetType, UUID targetId, List<FieldChange> changes) {
        Instant now = Instant.now(clock);
        for (FieldChange change : changes) {
            repository.save(new ChangeLogEntry(now, actorId, targetType, targetId,
                    change.field(), change.oldValue(), change.newValue()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoggedChange> history(String targetType, UUID targetId) {
        return repository.findByTargetTypeAndTargetIdOrderByChangedAtAscIdAsc(targetType, targetId).stream()
                .map(entry -> new LoggedChange(entry.getChangedAt(), entry.getActorId(), entry.getField(),
                        entry.getOldValue(), entry.getNewValue()))
                .toList();
    }
}
