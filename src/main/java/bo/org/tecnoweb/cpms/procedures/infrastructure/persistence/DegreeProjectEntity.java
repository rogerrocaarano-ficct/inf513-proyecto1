package bo.org.tecnoweb.cpms.procedures.infrastructure.persistence;

import bo.org.tecnoweb.cpms.procedures.domain.DegreeProject;
import bo.org.tecnoweb.cpms.procedures.domain.ProjectProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/** Maps a degree project, the stored file of its initial profile and its tutor (UC-003, issue #9). */
@Entity
@Table(name = "degree_project")
class DegreeProjectEntity {
    @Id
    private UUID id;
    private UUID procedureId;
    private String title;
    @Column(name = "profile_stored_file_id", nullable = false, unique = true)
    private UUID profileStoredFileId;
    private boolean profileApproved;
    private UUID tutorId;

    /** Required by JPA. */
    protected DegreeProjectEntity() {}

    /** Maps a newly registered aggregate. */
    DegreeProjectEntity(DegreeProject project) {
        id = project.getId();
        procedureId = project.getProcedureId();
        title = project.getTitle();
        profileStoredFileId = project.getProfile().storedFileId();
        profileApproved = project.getProfile().approved();
        updateTutor(project);
    }

    /** Maps the stored project without generating events. */
    DegreeProject toDomain() {
        return DegreeProject.rehydrate(id, procedureId, title,
                new ProjectProfile(profileStoredFileId, profileApproved), tutorId);
    }

    /** Updates the tutor without changing the stored evaluation. */
    void updateTutor(DegreeProject project) {
        tutorId = project.getTutorId();
    }
}
