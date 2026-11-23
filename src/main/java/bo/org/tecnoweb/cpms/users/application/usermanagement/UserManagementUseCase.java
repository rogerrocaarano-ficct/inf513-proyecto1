package bo.org.tecnoweb.cpms.users.application.usermanagement;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.RolePermission;
import bo.org.tecnoweb.cpms.users.domain.User;
import bo.org.tecnoweb.cpms.users.domain.UserDisabledEvent;
import bo.org.tecnoweb.cpms.users.domain.UserEnabledEvent;
import bo.org.tecnoweb.cpms.users.domain.UserNotAuthorizedException;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import bo.org.tecnoweb.cpms.users.domain.UserSignedUpEvent;
import java.util.List;
import java.util.Map;

/**
 * UC-001 Gestión de usuarios. Each flow of the use case is one method: {@link #signUpUser} (basic
 * flow), {@link #listUsers} (A3), {@link #disableUser} and {@link #enableUser} (A2).
 *
 * <p>In every flow the requester must have an account and it must be active (E1, FR-032).
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
     * @throws UserNotAuthorizedException if the sender has no account, its account is disabled, or its
     *                                    role may not create accounts of the requested role (E1)
     * @throws DomainValidationException  if the role is unknown, any field is invalid, or the email
     *                                    address or code already belong to another account
     */
    public void signUpUser(String requesterEmail, SignUpUserCommand command) {
        User requester = requireActiveRequester(requesterEmail);
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
     * <p>The permission is checked before reading the accounts. The list includes disabled accounts and
     * is sent by the notifier after the commit; an empty list is not an error.
     *
     * @param requesterEmail email address of the sender of the command
     * @param query          the role to list
     * @throws UserNotAuthorizedException if the sender has no account, its account is disabled, or its
     *                                    role may not list accounts of the requested role (E1)
     * @throws DomainValidationException  if the role is empty or unknown
     */
    public void listUsers(String requesterEmail, ListUsersQuery query) {
        User requester = requireActiveRequester(requesterEmail);
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

    /**
     * Disables an account (alternate flow A2 "Desactivar/Activar cuenta", {@code usuarios.desactivar}).
     *
     * <p>The account keeps its data. The notifier tells the requester and the account after the commit.
     *
     * @param requesterEmail email address of the sender of the command
     * @param userEmail      email address of the account to disable
     * @throws UserNotAuthorizedException if the sender has no account, its account is disabled, or its
     *                                    role may not change the status of accounts of that role (E1)
     * @throws DomainValidationException  if no account has that address, it is the requester's own
     *                                    account, it is the only active administrator (FR-038), or it is
     *                                    already disabled
     */
    public void disableUser(String requesterEmail, String userEmail) {
        User requester = requireActiveRequester(requesterEmail);
        User user = requireAffectedUser(requester, userEmail);
        // Only an active administrator may disable an administrator, so the only active one can only be
        // disabled by itself: FR-038 goes first so that case gets its own message.
        if (user.getRole() == Role.SYSTEM_ADMIN && user.isActive() && activeAdmins() == 1) {
            throw emailError("No se puede desactivar la única cuenta de administrador activa.");
        }
        if (user.getId().equals(requester.getId())) {
            throw emailError("No puede desactivar su propia cuenta.");
        }

        user.disable(requester);
        users.update(user);
        user.pullEvents().forEach(event -> {
            if (event instanceof UserDisabledEvent disabled) {
                notifier.notifyUserDisabled(disabled);
            }
        });
    }

    /**
     * Enables a disabled account again (alternate flow A2 "Desactivar/Activar cuenta", {@code
     * usuarios.activar}).
     *
     * <p>The notifier tells the requester and the account after the commit.
     *
     * @param requesterEmail email address of the sender of the command
     * @param userEmail      email address of the account to enable
     * @throws UserNotAuthorizedException if the sender has no account, its account is disabled, or its
     *                                    role may not change the status of accounts of that role (E1)
     * @throws DomainValidationException  if no account has that address or it is already active
     */
    public void enableUser(String requesterEmail, String userEmail) {
        User requester = requireActiveRequester(requesterEmail);
        User user = requireAffectedUser(requester, userEmail);

        user.enable(requester);
        users.update(user);
        user.pullEvents().forEach(event -> {
            if (event instanceof UserEnabledEvent enabled) {
                notifier.notifyUserEnabled(enabled);
            }
        });
    }

    /**
     * Returns the requester's account, which must exist and be active (E1, FR-032).
     */
    private User requireActiveRequester(String requesterEmail) {
        User requester = users.getByEmail(requesterEmail)
                .orElseThrow(() -> UserNotAuthorizedException.notRegistered(requesterEmail));
        if (!requester.isActive()) {
            throw UserNotAuthorizedException.disabled(requester.getEmailAddress());
        }
        return requester;
    }

    /**
     * Returns the account whose status changes, after checking that the requester may change the status
     * of accounts of its role.
     */
    private User requireAffectedUser(User requester, String userEmail) {
        String email = userEmail == null ? "" : userEmail.strip();
        User user = users.getByEmail(email)
                .orElseThrow(() -> emailError("No existe una cuenta con el correo " + email + "."));
        RolePermission permission = RolePermission.toChangeStatus(user.getRole());
        if (!requester.hasPermissionTo(permission)) {
            throw new UserNotAuthorizedException(requester.getEmailAddress(), permission.getLabel());
        }
        return user;
    }

    private long activeAdmins() {
        return users.getByRole(Role.SYSTEM_ADMIN).stream().filter(User::isActive).count();
    }

    private static DomainValidationException emailError(String message) {
        return new DomainValidationException(Map.of(User.EMAIL, message));
    }

    private static ListUsersQueryItem toListItem(User user) {
        return new ListUsersQueryItem(user.getFullName(), user.getPhoneNumber(), user.getUagrmCode(),
                user.getEmailAddress(), user.getRole().getLabel(), user.getCi(), user.isActive());
    }
}
