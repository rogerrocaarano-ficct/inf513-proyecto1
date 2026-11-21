package bo.org.tecnoweb.cpms.users.application;

import bo.org.tecnoweb.cpms.users.domain.UserSignedUpEvent;

/**
 * Outgoing port of UC-001: how the use case answers the requester and notifies other users.
 *
 * <p>Its implementation, in the infrastructure, sends the replies to the requester and the notices
 * to other users by email, only after the use case's transaction commits.
 */
public interface UserManagementNotifier {

    /**
     * Confirms to the requester that an account was created and notifies the new account (FR-035).
     *
     * @param event the created account and its requester
     */
    void notifyUserSignedUp(UserSignedUpEvent event);

    /**
     * Sends to the requester the list of accounts of {@code usuarios.listar[role]} (UC-001 A3).
     *
     * @param queryResult the accounts and the requester
     */
    void sendUserList(ListUsersQueryResult queryResult);
}
