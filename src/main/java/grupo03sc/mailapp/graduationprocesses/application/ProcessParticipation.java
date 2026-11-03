package grupo03sc.mailapp.graduationprocesses.application;

import grupo03sc.mailapp.graduationprocesses.domain.GraduationProcess;
import grupo03sc.mailapp.graduationprocesses.domain.GraduationProcessStatus;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The graduation processes in progress in which a user account takes part (FR-034): as the
 * student, or as tutor or tribunal member (designated by UC-005). It only uses the repository of
 * processes, so that Users, which calls it, is not called back.
 */
@Component
@Transactional
class ProcessParticipation implements GraduationParticipation {

    private final GraduationProcessRepository processes;

    ProcessParticipation(GraduationProcessRepository processes) {
        this.processes = processes;
    }

    @Override
    public List<GraduationProcessSummary> markPendingReassignment(UUID userAccountId, Set<ParticipantKind> kinds) {
        Map<UUID, GraduationProcess> found = new LinkedHashMap<>();
        if (kinds.contains(ParticipantKind.STUDENT)) {
            processes.findByStudentAccountIdAndStatus(userAccountId, GraduationProcessStatus.IN_PROGRESS)
                    .ifPresent(process -> found.put(process.getId(), process));
        }
        Set<String> professorKinds = kinds.stream().filter(kind -> kind != ParticipantKind.STUDENT)
                .map(Enum::name).collect(Collectors.toSet());
        if (!professorKinds.isEmpty()) {
            processes.findInProgressByParticipant(userAccountId, professorKinds)
                    .forEach(process -> found.putIfAbsent(process.getId(), process));
        }
        found.values().forEach(GraduationProcess::markPendingReassignment);
        return found.values().stream()
                .map(process -> new GraduationProcessSummary(process.getId(), process.getStudentAccountId()))
                .toList();
    }
}
