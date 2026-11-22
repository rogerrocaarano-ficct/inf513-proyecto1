package bo.org.tecnoweb.cpms.procedures.application;

import bo.org.tecnoweb.cpms.mail.domain.File;

/** Initial profile submission (UC-003 A1, FR-004). */
public record RegisterInitialProfileCommand(String title, File attachment) {}
