package grupo03sc.mailapp.graduationprocesses.application;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Interface provided by GraduationProcesses: the graduation processes in progress in which a user
 * account takes part.
 */
public interface GraduationParticipation {

    /**
     * Marks as pending reassignment the processes in progress where the account takes part with any
     * of the given kinds, and returns them.
     */
    List<GraduationProcessSummary> markPendingReassignment(UUID userAccountId, Set<ParticipantKind> kinds);
}
