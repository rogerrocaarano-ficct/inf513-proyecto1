package bo.org.tecnoweb.cpms.users.domain;

import bo.org.tecnoweb.cpms.common.DomainEvent;
import java.time.Instant;

/**
 * A user account was disabled by another user (UC-001 A2).
 *
 * @param disabledUserEmail     email address of the disabled account
 * @param disabledUserFullName  full name of the disabled account
 * @param occurredOn            moment the account was disabled
 * @param requesterUserFullName full name of the user who disabled the account
 * @param requesterUserEmail    email address of the user who disabled the account
 */
public record UserDisabledEvent(
        String disabledUserEmail,
        String disabledUserFullName,
        Instant occurredOn,
        String requesterUserFullName,
        String requesterUserEmail) implements DomainEvent {
}
