package bo.org.tecnoweb.cpms.procedures.domain;

import bo.org.tecnoweb.cpms.storage.domain.StoredFile;
import java.util.Objects;
import java.util.UUID;

/**
 * The initial profile and its recorded approval (UC-003, FR-004, FR-005). The PDF is kept by the
 * file storage; the profile keeps the identifier of its stored file (issue #9).
 *
 * @param storedFileId the stored file with the PDF of the profile
 * @param approved     whether the workshop teacher approved it
 */
public record ProjectProfile(UUID storedFileId, boolean approved) {

    /**
     * Checks the stored file.
     *
     * @throws NullPointerException if {@code storedFileId} is {@code null}
     */
    public ProjectProfile {
        Objects.requireNonNull(storedFileId);
    }

    /**
     * Creates a profile awaiting evaluation, which adopts a stored PDF already validated with
     * {@link PdfDocument#require}.
     *
     * @param file the stored file with the PDF
     * @return the pending profile
     */
    public static ProjectProfile pending(StoredFile file) {
        return new ProjectProfile(file.getId(), false);
    }
}
