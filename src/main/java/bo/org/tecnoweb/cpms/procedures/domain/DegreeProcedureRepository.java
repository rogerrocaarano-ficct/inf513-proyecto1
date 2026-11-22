package bo.org.tecnoweb.cpms.procedures.domain;

import java.util.Optional;
import java.util.UUID;

/** Persistence port for graduation procedures (UC-002). */
public interface DegreeProcedureRepository {
    /** Inserts a new aggregate and returns it with its generated reference. */
    DegreeProcedure insert(DegreeProcedure procedure);

    /** Checks for another ongoing procedure for the student (FR-056). */
    boolean existsOpenForStudent(UUID studentId);

    /** Finds and locks the ongoing procedure identified by its public reference (FR-055). */
    Optional<DegreeProcedure> getOpenByReferenceForUpdate(long reference);

    /** Saves document changes made to an existing aggregate (FR-055). */
    void update(DegreeProcedure procedure);
}
