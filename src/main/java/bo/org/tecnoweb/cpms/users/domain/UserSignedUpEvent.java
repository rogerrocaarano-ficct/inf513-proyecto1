package bo.org.tecnoweb.cpms.users.domain;

import bo.org.tecnoweb.cpms.common.DomainEvent;
import java.time.Instant;

/**
 * A user account was created by another user.
 *
 * @param signedUpUser          email address of the new account
 * @param signedUpUserFullName  full name of the new account
 * @param signedUpUserRole      label of the role of the new account, e.g. {@code docente}
 * @param occurredOn            moment the account was created
 * @param requesterUserFullName full name of the user who created the account
 * @param requesterUserEmail    email address of the user who created the account
 */
public record UserSignedUpEvent(
        String signedUpUser,
        String signedUpUserFullName,
        String signedUpUserRole,
        Instant occurredOn,
        String requesterUserFullName,
        String requesterUserEmail) implements DomainEvent {
}
