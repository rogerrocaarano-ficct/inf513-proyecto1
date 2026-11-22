package bo.org.tecnoweb.cpms.procedures.infrastructure.persistence;

import bo.org.tecnoweb.cpms.procedures.domain.DegreeProject;
import bo.org.tecnoweb.cpms.procedures.domain.ProjectProfile;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/** Maps a degree project, its initial profile and tutor (UC-003). */
@Entity
@Table(name = "degree_project")
class DegreeProjectEntity {
    @Id
    private UUID id;
    private UUID procedureId;
    private String title;
    private String profileFilename;
    private byte[] profileContent;
    private boolean profileApproved;
    private UUID tutorId;

    /** Required by JPA. */
    protected DegreeProjectEntity() {}

    /** Maps a newly registered aggregate. */
    DegreeProjectEntity(DegreeProject project) {
        id = project.getId();
        procedureId = project.getProcedureId();
        title = project.getTitle();
        profileFilename = project.getProfile().filename();
        profileContent = project.getProfile().content();
        profileApproved = project.getProfile().approved();
        updateTutor(project);
    }

    /** Maps the stored project without generating events. */
    DegreeProject toDomain() {
        return DegreeProject.rehydrate(id, procedureId, title,
                new ProjectProfile(profileFilename, profileContent, profileApproved), tutorId);
    }

    /** Updates the tutor without changing the stored evaluation. */
    void updateTutor(DegreeProject project) {
        tutorId = project.getTutorId();
    }
}
