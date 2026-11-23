package bo.org.tecnoweb.cpms.procedures.domain;

import bo.org.tecnoweb.cpms.common.DomainEvent;
import java.time.Instant;

/**
 * Records a graduation procedure opening (UC-002, FR-054).
 *
 * @param requester   the address of the account that opened it
 * @param student     the address of the student
 * @param studentName the full name of the student
 * @param reference   the procedure reference, for example {@code 2026-2-218001234}
 * @param occurredOn  when it was opened
 */
public record DegreeProcedureOpenedEvent(String requester, String student, String studentName, String reference,
        Instant occurredOn) implements DomainEvent {}
