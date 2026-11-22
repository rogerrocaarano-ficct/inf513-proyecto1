package bo.org.tecnoweb.cpms.procedures.domain;

import bo.org.tecnoweb.cpms.common.DomainEvent;
import java.time.Instant;

/** Carries the tutor designation to the student (UC-003, FR-020). */
public record DegreeProjectTutorAssignedEvent(String student, String reference, String tutorName, Instant occurredOn)
        implements DomainEvent {}
