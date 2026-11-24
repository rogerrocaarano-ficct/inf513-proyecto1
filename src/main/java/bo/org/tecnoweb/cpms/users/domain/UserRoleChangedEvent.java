package bo.org.tecnoweb.cpms.users.domain;

import bo.org.tecnoweb.cpms.common.DomainEvent;
import java.time.Instant;

/**
 * The role of a user account was changed by another user (UC-001 A1, FR-031).
 *
 * @param changedUserEmail      email address of the account whose role changed
 * @param changedUserFullName   full name of the account whose role changed
 * @param previousRole          label of the role the account had, e.g. {@code docente}
 * @param newRole               label of the role the account has now
 * @param occurredOn            moment the role changed
 * @param requesterUserFullName full name of the user who changed the role
 * @param requesterUserEmail    email address of the user who changed the role
 */
public record UserRoleChangedEvent(
        String changedUserEmail,
        String changedUserFullName,
        String previousRole,
        String newRole,
        Instant occurredOn,
        String requesterUserFullName,
        String requesterUserEmail) implements DomainEvent {
}
