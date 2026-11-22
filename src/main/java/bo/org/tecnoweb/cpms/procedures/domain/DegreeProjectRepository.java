package bo.org.tecnoweb.cpms.procedures.domain;

import java.util.Optional;
import java.util.UUID;

/** Persistence port for degree projects (UC-003). */
public interface DegreeProjectRepository {
    /** Finds the project attached to the procedure. */
    Optional<DegreeProject> getByProcedureId(UUID procedureId);

    /** Inserts a newly registered project. */
    void insert(DegreeProject project);

    /** Saves the tutor designation. */
    void update(DegreeProject project);
}
