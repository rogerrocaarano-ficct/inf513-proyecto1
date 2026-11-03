package grupo03sc.mailapp.graduationprocesses.application;

import grupo03sc.mailapp.graduationprocesses.domain.GraduationProcess;
import grupo03sc.mailapp.graduationprocesses.domain.GraduationProcessStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

/** Persistence port of GraduationProcesses, implemented by Spring Data JPA. */
public interface GraduationProcessRepository extends Repository<GraduationProcess, UUID> {

    GraduationProcess saveAndFlush(GraduationProcess process);

    boolean existsByStudentAccountIdAndStatus(UUID studentAccountId, GraduationProcessStatus status);

    Optional<GraduationProcess> findByStudentAccountIdAndStatus(UUID studentAccountId, GraduationProcessStatus status);

    /** The processes in progress where the account is designated with any of the kinds (UC-005 fills them). */
    @Query(value = "select g.* from graduation_processes g where g.status = 'IN_PROGRESS' and exists ("
            + "select 1 from graduation_process_participants p where p.graduation_process_id = g.id"
            + " and p.user_account_id = :accountId and p.kind in (:kinds))", nativeQuery = true)
    List<GraduationProcess> findInProgressByParticipant(UUID accountId, Collection<String> kinds);
}
