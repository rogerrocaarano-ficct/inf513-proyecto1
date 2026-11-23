package bo.org.tecnoweb.cpms.procedures.infrastructure.persistence;

import bo.org.tecnoweb.cpms.procedures.domain.DegreeProject;
import bo.org.tecnoweb.cpms.procedures.domain.ProjectProfile;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.hibernate.annotations.ListIndexBase;

/**
 * Maps a degree project, the versions of its profile (UC-004) and its tutor (UC-003). The versions are
 * values of the project, in table {@code degree_project_profile_version}; the order of the list is
 * the {@code version} column, numbered from 1 like the domain.
 */
@Entity
@Table(name = "degree_project")
class DegreeProjectEntity {
    @Id
    private UUID id;
    private UUID procedureId;
    private String title;
    private UUID tutorId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "degree_project_profile_version", joinColumns = @JoinColumn(name = "project_id"))
    @OrderColumn(name = "version")
    @ListIndexBase(1)
    private List<DegreeProjectProfileVersion> profiles = new ArrayList<>();

    /** Required by JPA. */
    protected DegreeProjectEntity() {}

    /** Maps a newly registered aggregate. */
    DegreeProjectEntity(DegreeProject project) {
        id = project.getId();
        procedureId = project.getProcedureId();
        update(project);
    }

    /** Maps the stored project without generating events; each version takes its number from the order. */
    DegreeProject toDomain() {
        List<ProjectProfile> versions = IntStream.range(0, profiles.size())
                .mapToObj(index -> profiles.get(index).toDomain(index + 1)).toList();
        return DegreeProject.rehydrate(id, procedureId, title, versions, tutorId);
    }

    /** Copies the title, the profile versions and the tutor of the aggregate (FR-003, FR-023). */
    void update(DegreeProject project) {
        title = project.getTitle();
        tutorId = project.getTutorId();
        for (ProjectProfile version : project.getProfiles()) {
            var row = new DegreeProjectProfileVersion(version);
            int index = version.version() - 1;
            if (index < profiles.size()) {
                profiles.set(index, row);
            } else {
                profiles.add(row);
            }
        }
    }
}
