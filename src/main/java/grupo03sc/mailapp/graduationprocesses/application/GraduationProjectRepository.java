package grupo03sc.mailapp.graduationprocesses.application;

import grupo03sc.mailapp.graduationprocesses.domain.GraduationProject;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.Repository;

/** Persistence port of the graduation projects, implemented by Spring Data JPA (UC-004). */
public interface GraduationProjectRepository extends Repository<GraduationProject, UUID> {

    GraduationProject saveAndFlush(GraduationProject project);

    Optional<GraduationProject> findByGraduationProcessId(UUID graduationProcessId);
}
