package grupo03sc.mailapp.graduationprocesses.application;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Placeholder until graduation processes exist: no account takes part in any process yet (D6).
 * The use case that creates graduation processes replaces it.
 */
@Component
class NoGraduationProcessesParticipation implements GraduationParticipation {

    @Override
    public List<GraduationProcessSummary> markPendingReassignment(UUID userAccountId, Set<ParticipantKind> kinds) {
        return List.of();
    }
}
