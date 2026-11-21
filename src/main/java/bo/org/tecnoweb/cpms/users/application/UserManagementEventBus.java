package bo.org.tecnoweb.cpms.users.application;

import bo.org.tecnoweb.cpms.users.domain.UserSignedUpEvent;

/**
 * Outgoing port of UC-001: where the use case publishes what happened.
 *
 * <p>Its implementation, in the infrastructure, sends the success reply to the requester and the
 * notices to other users by email, only after the use case's transaction commits.
 */
public interface UserManagementEventBus {

    /**
     * Publishes that an account was created.
     *
     * @param event the created account and its requester
     */
    void notifyUserSignedUpEvent(UserSignedUpEvent event);
}
