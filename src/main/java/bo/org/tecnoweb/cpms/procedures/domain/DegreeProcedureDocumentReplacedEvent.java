package bo.org.tecnoweb.cpms.procedures.domain;

import bo.org.tecnoweb.cpms.common.DomainEvent;
import java.time.Instant;

/** Records an initial document replacement (UC-002, FR-055). */
public record DegreeProcedureDocumentReplacedEvent(String requester, String reference, String documentLabel,
        Instant occurredOn) implements DomainEvent {}
