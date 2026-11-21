package bo.org.tecnoweb.cpms.users.application;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.RolePermission;
import bo.org.tecnoweb.cpms.users.domain.User;
import bo.org.tecnoweb.cpms.users.domain.UserNotAuthorizedException;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import bo.org.tecnoweb.cpms.users.domain.UserSignedUpEvent;
import java.util.List;

/**
 * UC-001 Gestión de usuarios. Each flow of the use case is one method: {@link #signUpUser} (basic
 * flow) and {@link #listUsers} (A3).
 */
public class UserManagementUseCase {

    private final UserRepository users;
    private final UserManagementNotifier notifier;

    /**
     * Creates the use case.
     *
     * @param users    storage of the accounts
     * @param notifier where the use case sends its replies and notices
     */
    public UserManagementUseCase(UserRepository users, UserManagementNotifier notifier) {
        this.users = users;
        this.notifier = notifier;
    }

    /**
     * Creates an account (basic flow "Crear Cuenta").
     *
     * <p>Runs inside the caller's transaction: the account is inserted and the event published there,
     * and the notifier sends the emails after the commit.
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
                notifier.notifyUserSignedUp(signedUp);
            }
        });
    }

    /**
     * Lists the accounts of a role to the requester (alternate flow A3 "Listar usuarios").
     *
     * <p>The permission is checked before reading the accounts. The list is sent by the notifier
     * after the commit; an empty list is not an error.
     *
     * @param requesterEmail email address of the sender of the command
     * @param query          the role to list
     * @throws UserNotAuthorizedException if the sender has no account, or its role may not list
     *                                    accounts of the requested role (E1)
     * @throws DomainValidationException  if the role is empty or unknown
     */
    public void listUsers(String requesterEmail, ListUsersQuery query) {
        User requester = users.getByEmail(requesterEmail)
                .orElseThrow(() -> UserNotAuthorizedException.notRegistered(requesterEmail));
        Role role = Role.fromLabel(query.role());
        RolePermission permission = RolePermission.toList(role);
        if (!requester.hasPermissionTo(permission)) {
            throw new UserNotAuthorizedException(requester.getEmailAddress(), permission.getLabel());
        }

        List<ListUsersQueryItem> items = users.getByRole(role).stream()
                .map(UserManagementUseCase::toListItem)
                .toList();

        var queryResult = new ListUsersQueryResult(items, role.getLabel(), requester.getEmailAddress());
        notifier.sendUserList(queryResult);
    }

    private static ListUsersQueryItem toListItem(User user) {
        return new ListUsersQueryItem(user.getFullName(), user.getPhoneNumber(), user.getUagrmCode(),
                user.getEmailAddress(), user.getRole().getLabel(), user.getCi());
    }
}
