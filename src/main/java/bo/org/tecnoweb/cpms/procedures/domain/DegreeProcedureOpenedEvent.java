package bo.org.tecnoweb.cpms.procedures.domain;

import bo.org.tecnoweb.cpms.common.DomainEvent;
import java.time.Instant;

/** Records a graduation procedure opening (UC-002, FR-054). */
public record DegreeProcedureOpenedEvent(String requester, String student, String studentName,
        Instant occurredOn) implements DomainEvent {}
