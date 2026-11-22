package bo.org.tecnoweb.cpms.procedures.application;

import bo.org.tecnoweb.cpms.mail.domain.File;

/** Input of the UC-002 replacement flow (FR-055). */
public record ReplaceInitialDocumentCommand(String reference, File attachment) {}
