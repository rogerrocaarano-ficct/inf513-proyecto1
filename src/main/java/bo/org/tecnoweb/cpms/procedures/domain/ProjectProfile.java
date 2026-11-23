package bo.org.tecnoweb.cpms.procedures.domain;

import bo.org.tecnoweb.cpms.storage.domain.StoredFile;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * One version of the project profile and its evaluation (UC-004, FR-004, FR-005, FR-022, FR-023). The
 * versions are numbered by the project, from 1; the PDF of each one is an ordinary stored file, not
 * chained to the previous one in the file storage.
 *
 * @param version      the number of the version within its project, from 1
 * @param storedFileId the stored file with the PDF of this version
 * @param title        the project title proposed in this version
 * @param status       the state of the version
 * @param observations the observations of the workshop teacher, or {@code null} if there are none
 * @param evaluatedOn  when the version was evaluated, or {@code null} while it is pending
 */
public record ProjectProfile(int version, UUID storedFileId, String title, ProjectProfileStatus status,
        String observations, Instant evaluatedOn) {

    /**
     * Checks the version.
     *
     * @throws NullPointerException     if the file, title or status is {@code null}
     * @throws IllegalArgumentException if the number is below 1, a pending version has an evaluation, an
     *                                  evaluated one has no date, or an observed or rejected one has no
     *                                  observations
     */
    public ProjectProfile {
        if (version < 1) {
            throw new IllegalArgumentException("Profile versions are numbered from 1");
        }
        Objects.requireNonNull(storedFileId);
        Objects.requireNonNull(title);
        Objects.requireNonNull(status);
        if ((status == ProjectProfileStatus.PENDING) != (evaluatedOn == null)) {
            throw new IllegalArgumentException("Only an evaluated profile has an evaluation date");
        }
        if (status.allowsNewVersion() && (observations == null || observations.isBlank())) {
            throw new IllegalArgumentException("An observed or rejected profile needs observations");
        }
    }

    /**
     * Creates a version awaiting evaluation, which adopts a stored PDF already validated with
     * {@link PdfDocument#require}.
     *
     * @param version the number of the version within its project, from 1
     * @param file    the stored file with the PDF
     * @param title   the project title proposed in this version
     * @return the pending version
     */
    public static ProjectProfile pending(int version, StoredFile file, String title) {
        return new ProjectProfile(version, file.getId(), title, ProjectProfileStatus.PENDING, null, null);
    }

    /**
     * Returns this version evaluated.
     *
     * @param result       the result of the evaluation
     * @param observations the observations, or {@code null} for an approval
     * @param evaluatedOn  when it was evaluated
     * @return the evaluated version
     */
    ProjectProfile evaluated(ProjectProfileStatus result, String observations, Instant evaluatedOn) {
        return new ProjectProfile(version, storedFileId, title, result, observations, evaluatedOn);
    }
}
