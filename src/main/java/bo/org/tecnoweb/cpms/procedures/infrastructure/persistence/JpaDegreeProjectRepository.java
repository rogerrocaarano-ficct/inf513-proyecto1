package bo.org.tecnoweb.cpms.procedures.infrastructure.persistence;

import bo.org.tecnoweb.cpms.procedures.domain.DegreeProject;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** JPA adapter for the degree project aggregate (UC-003, UC-004). */
@Repository
@Transactional
class JpaDegreeProjectRepository implements DegreeProjectRepository {
    private final SpringDataDegreeProjectRepository projects;

    /** Receives the Spring Data project repository. */
    JpaDegreeProjectRepository(SpringDataDegreeProjectRepository projects) {
        this.projects = projects;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<DegreeProject> getByProcedureId(UUID procedureId) {
        return projects.findByProcedureId(procedureId).map(DegreeProjectEntity::toDomain);
    }

    /** {@inheritDoc} */
    @Override
    public void insert(DegreeProject project) {
        projects.save(new DegreeProjectEntity(project));
    }

    /** {@inheritDoc} */
    @Override
    public void update(DegreeProject project) {
        projects.findById(project.getId()).orElseThrow().update(project);
    }
}
