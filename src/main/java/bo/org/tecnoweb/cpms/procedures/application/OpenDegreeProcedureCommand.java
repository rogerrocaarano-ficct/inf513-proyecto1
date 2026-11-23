package bo.org.tecnoweb.cpms.procedures.application;

import java.util.List;
import java.util.UUID;

/**
 * Input of the UC-002 opening flow (FR-001, FR-002, FR-056).
 *
 * @param code          the registration number of the student account, created beforehand by UC-001
 * @param attachmentIds the stored files of the four initial PDFs, in message order (issue #9)
 */
public record OpenDegreeProcedureCommand(String code, List<UUID> attachmentIds) {}
