package bo.org.tecnoweb.cpms.procedures.domain;

import java.util.Optional;
import java.util.UUID;

/** Persistence port for graduation procedures (UC-002). */
public interface DegreeProcedureRepository {
    /**
     * Inserts a newly opened procedure.
     *
     * @param procedure the procedure, with its reference
     */
    void insert(DegreeProcedure procedure);

    /** Checks for another ongoing procedure for the student (FR-056). */
    boolean existsOpenForStudent(UUID studentId);

    /**
     * Tells whether a procedure, ongoing or closed, has a reference; since the reference is built from
     * the semester and the student, this tells whether the student already opened one in that
     * semester (UC-002).
     *
     * @param reference the reference, for example {@code 2026-2-218001234}
     * @return {@code true} if a procedure has it
     */
    boolean existsByReference(String reference);

    /**
     * Finds and locks the ongoing procedure identified by its reference (FR-055, UC-003).
     *
     * @param reference the reference, for example {@code 2026-2-218001234}
     * @return the procedure, or empty if no ongoing procedure has it
     */
    Optional<DegreeProcedure> getOpenByReferenceForUpdate(String reference);

    /** Finds and locks the student's ongoing procedure (UC-003, FR-004). */
    Optional<DegreeProcedure> getOpenByStudentForUpdate(UUID studentId);

    /** Saves document changes made to an existing aggregate (FR-055). */
    void update(DegreeProcedure procedure);
}
