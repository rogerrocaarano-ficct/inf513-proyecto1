package bo.org.tecnoweb.cpms.procedures.domain;

import bo.org.tecnoweb.cpms.common.DomainEvent;
import java.time.Instant;

/**
 * Carries a submitted profile to the workshop teacher (UC-004, FR-057).
 *
 * @param student    the email of the student
 * @param workshop   the email of the workshop teacher
 * @param reference  the reference of the procedure
 * @param title      the project title of the version
 * @param profile    the submitted version, with its number (FR-023)
 * @param occurredOn when it was submitted
 */
public record DegreeProjectProfileRegisteredEvent(String student, String workshop, String reference, String title,
        ProjectProfile profile, Instant occurredOn) implements DomainEvent {}
