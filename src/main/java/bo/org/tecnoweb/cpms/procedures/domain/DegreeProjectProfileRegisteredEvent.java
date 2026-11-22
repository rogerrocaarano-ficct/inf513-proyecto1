package bo.org.tecnoweb.cpms.procedures.domain;

import bo.org.tecnoweb.cpms.common.DomainEvent;
import java.time.Instant;

/** Carries the initial profile to the workshop teacher (UC-003, FR-057). */
public record DegreeProjectProfileRegisteredEvent(String student, String workshop, String reference, String title,
        ProjectProfile profile, Instant occurredOn) implements DomainEvent {}
