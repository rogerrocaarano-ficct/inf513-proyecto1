package bo.org.tecnoweb.cpms.procedures.domain;

import bo.org.tecnoweb.cpms.common.DomainEvent;
import java.time.Instant;

/**
 * Carries the observations of the profile to the student (UC-004, FR-022, FR-060).
 *
 * @param student      the email of the student
 * @param reference    the reference of the procedure
 * @param title        the project title of the observed version
 * @param observations the observations of the workshop teacher
 * @param occurredOn   when it was observed
 */
public record DegreeProjectProfileObservedEvent(String student, String reference, String title, String observations,
        Instant occurredOn) implements DomainEvent {}
