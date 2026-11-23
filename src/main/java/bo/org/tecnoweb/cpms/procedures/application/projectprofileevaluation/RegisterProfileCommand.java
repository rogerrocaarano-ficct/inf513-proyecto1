package bo.org.tecnoweb.cpms.procedures.application.projectprofileevaluation;

import java.util.UUID;

/**
 * Profile submission, the first one or a new version (UC-004 basic flow, FR-004, FR-023).
 *
 * @param title        the project title
 * @param attachmentId the stored file of the profile PDF
 */
public record RegisterProfileCommand(String title, UUID attachmentId) {}
