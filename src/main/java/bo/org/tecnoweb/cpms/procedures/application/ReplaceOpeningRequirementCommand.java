package bo.org.tecnoweb.cpms.procedures.application;

import java.util.UUID;

/**
 * Input of the UC-002 replacement of an opening requirement (FR-055).
 *
 * @param reference    the procedure reference, such as {@code 2026-2-218001234}
 * @param attachmentId the stored file of the new PDF, whose name tells the requirement (issue #9)
 */
public record ReplaceOpeningRequirementCommand(String reference, UUID attachmentId) {}
