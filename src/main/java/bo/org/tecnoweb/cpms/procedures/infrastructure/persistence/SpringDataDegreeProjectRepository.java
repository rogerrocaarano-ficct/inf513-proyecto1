package bo.org.tecnoweb.cpms.procedures.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** ORM queries for degree projects (UC-003, UC-004). */
interface SpringDataDegreeProjectRepository extends JpaRepository<DegreeProjectEntity, UUID> {
    /** Finds the project associated with a graduation procedure. */
    Optional<DegreeProjectEntity> findByProcedureId(UUID procedureId);
}
