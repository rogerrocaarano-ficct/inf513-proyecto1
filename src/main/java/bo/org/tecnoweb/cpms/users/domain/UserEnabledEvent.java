package bo.org.tecnoweb.cpms.users.domain;

import bo.org.tecnoweb.cpms.common.DomainEvent;
import java.time.Instant;

/**
 * A disabled user account was enabled again by another user (UC-001 A2).
 *
 * @param enabledUserEmail      email address of the enabled account
 * @param enabledUserFullName   full name of the enabled account
 * @param occurredOn            moment the account was enabled
 * @param requesterUserFullName full name of the user who enabled the account
 * @param requesterUserEmail    email address of the user who enabled the account
 */
public record UserEnabledEvent(
        String enabledUserEmail,
        String enabledUserFullName,
        Instant occurredOn,
        String requesterUserFullName,
        String requesterUserEmail) implements DomainEvent {
}
