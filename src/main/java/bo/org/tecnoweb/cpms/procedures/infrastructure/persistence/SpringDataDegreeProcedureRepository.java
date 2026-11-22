package bo.org.tecnoweb.cpms.procedures.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

/** ORM queries for UC-002. */
interface SpringDataDegreeProcedureRepository extends JpaRepository<DegreeProcedureEntity, Long> {
    /** Checks the ongoing procedure associated with a student (FR-056). */
    boolean existsByStudentIdAndClosedAtIsNull(UUID studentId);

    /** Locks a procedure that is still open for initial document replacement (FR-055). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<DegreeProcedureEntity> findForUpdateByReferenceAndClosedAtIsNull(long reference);
}
