package bo.org.tecnoweb.cpms.procedures.application;

import java.util.UUID;

/**
 * Initial profile submission (UC-003 A1, FR-004).
 *
 * @param title        the project title
 * @param attachmentId the stored file of the profile PDF (issue #9)
 */
public record RegisterInitialProfileCommand(String title, UUID attachmentId) {}
