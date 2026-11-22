package bo.org.tecnoweb.cpms.procedures.application;

/** Tutor designation request (UC-003, FR-003). */
public record AssignTutorCommand(String reference, String tutorEmail) {}
