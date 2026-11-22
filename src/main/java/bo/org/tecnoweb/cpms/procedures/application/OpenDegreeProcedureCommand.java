package bo.org.tecnoweb.cpms.procedures.application;

import java.util.List;
import java.util.UUID;

/**
 * Input of the UC-002 opening flow (FR-001, FR-002, FR-051).
 *
 * @param attachmentIds the stored files of the four initial PDFs, in message order (issue #9)
 */
public record OpenDegreeProcedureCommand(String firstName, String lastName, String secondLastName,
        String ci, String code, String email, String phoneNumber, List<UUID> attachmentIds) {}
