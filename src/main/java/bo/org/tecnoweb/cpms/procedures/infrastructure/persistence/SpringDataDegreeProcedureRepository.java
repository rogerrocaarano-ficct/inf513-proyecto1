package bo.org.tecnoweb.cpms.procedures.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

/** ORM queries for UC-002. */
interface SpringDataDegreeProcedureRepository extends JpaRepository<DegreeProcedureEntity, UUID> {
    /** Checks the ongoing procedure associated with a student (FR-056). */
    boolean existsByStudentIdAndClosedAtIsNull(UUID studentId);

    /** Tells whether a procedure, ongoing or closed, has a reference (UC-002). */
    boolean existsByReference(String reference);

    /** Locks a procedure that is still open by its reference (FR-055, UC-003, UC-004). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<DegreeProcedureEntity> findForUpdateByReferenceAndClosedAtIsNull(String reference);

    /** Locks the student's ongoing procedure before registering a profile (UC-004, FR-004). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<DegreeProcedureEntity> findForUpdateByStudentIdAndClosedAtIsNull(UUID studentId);
}
