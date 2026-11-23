package bo.org.tecnoweb.cpms.procedures.domain;

import bo.org.tecnoweb.cpms.common.DomainEvent;
import java.time.Instant;

/**
 * Carries the approval of the profile to the student and the director (UC-004, FR-005, FR-060, FR-061).
 *
 * @param student    the email of the student
 * @param director   the email of the active director, or {@code null} if there is none (FR-061)
 * @param reference  the reference of the procedure
 * @param title      the project title of the approved version
 * @param occurredOn when it was approved
 */
public record DegreeProjectProfileApprovedEvent(String student, String director, String reference, String title,
        Instant occurredOn) implements DomainEvent {}
