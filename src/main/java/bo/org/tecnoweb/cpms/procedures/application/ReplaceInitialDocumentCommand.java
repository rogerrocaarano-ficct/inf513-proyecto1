package bo.org.tecnoweb.cpms.procedures.application;

import java.util.UUID;

/**
 * Input of the UC-002 replacement flow (FR-055).
 *
 * @param reference    the procedure reference, such as {@code 2026-2-218001234}
 * @param attachmentId the stored file of the new PDF (issue #9)
 */
public record ReplaceInitialDocumentCommand(String reference, UUID attachmentId) {}
