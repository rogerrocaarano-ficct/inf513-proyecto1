package bo.org.tecnoweb.cpms.procedures.domain;

import bo.org.tecnoweb.cpms.common.DomainEvent;
import java.time.Instant;

/**
 * Carries the rejection of the profile to the student (UC-004, FR-022, FR-060).
 *
 * @param student      the email of the student
 * @param reference    the reference of the procedure
 * @param title        the project title of the rejected version
 * @param observations why the topic must change
 * @param occurredOn   when it was rejected
 */
public record DegreeProjectProfileRejectedEvent(String student, String reference, String title, String observations,
        Instant occurredOn) implements DomainEvent {}
