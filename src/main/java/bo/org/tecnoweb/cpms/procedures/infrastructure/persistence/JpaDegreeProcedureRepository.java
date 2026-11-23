package bo.org.tecnoweb.cpms.procedures.infrastructure.persistence;

import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedure;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** JPA adapter for the graduation procedure aggregate (UC-002). */
@Repository
@Transactional
class JpaDegreeProcedureRepository implements DegreeProcedureRepository {
    private final SpringDataDegreeProcedureRepository procedures;

    /** Receives the Spring Data repository used by the adapter. */
    JpaDegreeProcedureRepository(SpringDataDegreeProcedureRepository procedures) {
        this.procedures = procedures;
    }

    /** {@inheritDoc} */
    @Override
    public void insert(DegreeProcedure procedure) {
        procedures.save(new DegreeProcedureEntity(procedure));
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsOpenForStudent(UUID studentId) {
        return procedures.existsByStudentIdAndClosedAtIsNull(studentId);
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsByReference(String reference) {
        return procedures.existsByReference(reference);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<DegreeProcedure> getOpenByReferenceForUpdate(String reference) {
        return procedures.findForUpdateByReferenceAndClosedAtIsNull(reference).map(DegreeProcedureEntity::toDomain);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<DegreeProcedure> getOpenByStudentForUpdate(UUID studentId) {
        return procedures.findForUpdateByStudentIdAndClosedAtIsNull(studentId).map(DegreeProcedureEntity::toDomain);
    }

    /** {@inheritDoc} */
    @Override
    public void update(DegreeProcedure procedure) {
        procedures.findById(procedure.getId()).orElseThrow().updateRequirements(procedure);
    }
}
