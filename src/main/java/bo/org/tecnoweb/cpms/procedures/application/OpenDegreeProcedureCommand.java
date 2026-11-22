package bo.org.tecnoweb.cpms.procedures.application;

import bo.org.tecnoweb.cpms.mail.domain.File;
import java.util.List;

/** Input of the UC-002 opening flow (FR-001, FR-002, FR-051). */
public record OpenDegreeProcedureCommand(String firstName, String lastName, String secondLastName,
        String ci, String code, String email, String phoneNumber, List<File> attachments) {}
