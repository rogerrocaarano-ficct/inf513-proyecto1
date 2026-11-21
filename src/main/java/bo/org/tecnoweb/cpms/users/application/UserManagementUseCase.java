package bo.org.tecnoweb.cpms.users.application;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.User;
import bo.org.tecnoweb.cpms.users.domain.UserNotAuthorizedException;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import bo.org.tecnoweb.cpms.users.domain.UserSignedUpEvent;

/**
 * UC-001 Gestión de usuarios. Each flow of the use case is one method; for now only the basic flow
 * (create an account).
 */
public class UserManagementUseCase {

    private final UserRepository users;
    private final UserManagementEventBus eventBus;

    /**
     * Creates the use case.
     *
     * @param users    storage of the accounts
     * @param eventBus where the use case publishes what happened
     */
    public UserManagementUseCase(UserRepository users, UserManagementEventBus eventBus) {
        this.users = users;
        this.eventBus = eventBus;
    }

    /**
     * Creates an account (basic flow "Crear Cuenta").
     *
     * <p>Runs inside the caller's transaction: the account is inserted and the event published there,
     * and the event bus sends the emails after the commit.
     *
     * @param requesterEmail email address of the sender of the command
     * @param command        data of the new account
     * @throws UserNotAuthorizedException if the sender has no account, or its role may not create
     *                                    accounts of the requested role (E1)
     * @throws DomainValidationException  if the role is unknown, any field is invalid, or the email
     *                                    address or code already belong to another account
     */
    public void signUpUser(String requesterEmail, SignUpUserCommand command) {
        User requester = users.getByEmail(requesterEmail)
                .orElseThrow(() -> UserNotAuthorizedException.notRegistered(requesterEmail));
        Role role = Role.fromLabel(command.role());

        User user = User.builder()
                .withFirstName(command.firstName())
                .withLastName(command.lastName())
                .withSecondLastName(command.secondLastName())
                .withCi(command.ci())
                .withUagrmCode(command.code())
                .withEmailAddress(command.email())
                .withPhoneNumber(command.phoneNumber())
                .withRole(role)
                .signUp(requester);
        users.insert(user);

        user.pullEvents().forEach(event -> {
            if (event instanceof UserSignedUpEvent signedUp) {
                eventBus.notifyUserSignedUpEvent(signedUp);
            }
        });
    }
}
