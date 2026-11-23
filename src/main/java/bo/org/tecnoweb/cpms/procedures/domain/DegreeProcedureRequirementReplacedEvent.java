package bo.org.tecnoweb.cpms.procedures.domain;

import bo.org.tecnoweb.cpms.common.DomainEvent;
import java.time.Instant;

/**
 * Records that the document of an opening requirement was replaced (UC-002, FR-055).
 *
 * @param requester        the address of the account that replaced it
 * @param reference        the procedure reference, for example {@code 2026-2-218001234}
 * @param requirementLabel the Spanish name of the requirement
 * @param occurredOn       when it was replaced
 */
public record DegreeProcedureRequirementReplacedEvent(String requester, String reference, String requirementLabel,
        Instant occurredOn) implements DomainEvent {}
