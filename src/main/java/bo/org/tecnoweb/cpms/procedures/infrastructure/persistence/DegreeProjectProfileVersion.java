package bo.org.tecnoweb.cpms.procedures.infrastructure.persistence;

import bo.org.tecnoweb.cpms.procedures.domain.ProjectProfile;
import bo.org.tecnoweb.cpms.procedures.domain.ProjectProfileStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.Instant;
import java.util.UUID;

/**
 * A row of {@code degree_project_profile_version}: one version of the profile of a project (UC-004).
 * Its number is the {@code version} column, mapped as the order of the list in
 * {@link DegreeProjectEntity}.
 */
@Embeddable
class DegreeProjectProfileVersion {
    @Column(name = "stored_file_id", nullable = false, unique = true)
    private UUID storedFileId;
    @Column(nullable = false)
    private String title;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectProfileStatus status;
    private String observations;
    private Instant evaluatedOn;

    /** Required by JPA. */
    protected DegreeProjectProfileVersion() {}

    /** Maps a version of the domain; its number is kept by the order of the list. */
    DegreeProjectProfileVersion(ProjectProfile profile) {
        storedFileId = profile.storedFileId();
        title = profile.title();
        status = profile.status();
        observations = profile.observations();
        evaluatedOn = profile.evaluatedOn();
    }

    /**
     * Maps the row back to the domain.
     *
     * @param version the number of the version, read from the {@code version} column
     * @return the version of the profile
     */
    ProjectProfile toDomain(int version) {
        return new ProjectProfile(version, storedFileId, title, status, observations, evaluatedOn);
    }
}
