package bo.org.tecnoweb.cpms.procedures.domain;

import bo.org.tecnoweb.cpms.mail.domain.File;

/** The initial profile and its recorded approval (UC-003, FR-004, FR-005). */
public record ProjectProfile(String filename, byte[] content, boolean approved) {
    /** Copies the file contents into the profile. */
    public ProjectProfile {
        content = content.clone();
    }

    /** Returns a copy of the stored contents. */
    @Override
    public byte[] content() {
        return content.clone();
    }

    /** Validates the PDF and creates a profile awaiting evaluation. */
    public static ProjectProfile pending(File file) {
        InitialDocument.validatePdf(file);
        return new ProjectProfile(file.getName(), file.getData(), false);
    }
}
