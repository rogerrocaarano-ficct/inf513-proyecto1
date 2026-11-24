package bo.org.tecnoweb.cpms.users.application;

import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.RolePermission;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description of one email command for the help reply (FR-066): its syntax, what it is for, what it
 * needs besides the arguments, and the permissions that allow using it.
 *
 * <p>Holding any one of {@code permissions} is enough to use the command. A command whose permission
 * depends on an argument, such as the role of the account to create, lists one permission per role.
 * An empty set means that every active account may use it.
 *
 * @param action      the command name, the {@code ACTION} of its handler
 * @param syntax      the subject with the name of each argument, e.g. {@code usuarios.listar[rol]}
 * @param purpose     what the command is for, in Spanish
 * @param notes       the attachments or the body text it needs, in Spanish; empty if none
 * @param permissions the permissions that allow using it, in declaration order
 */
public record CommandGuide(String action, String syntax, String purpose, String notes,
        Set<RolePermission> permissions) {

    /**
     * Checks the fields and keeps an unmodifiable copy of the permissions.
     *
     * @throws NullPointerException if a field or a permission is {@code null}
     */
    public CommandGuide {
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(syntax, "syntax");
        Objects.requireNonNull(purpose, "purpose");
        Objects.requireNonNull(notes, "notes");
        Objects.requireNonNull(permissions, "permissions");
        permissions.forEach(permission -> Objects.requireNonNull(permission, "permission"));
        permissions = permissions.isEmpty()
                ? Set.of()
                : Collections.unmodifiableSet(EnumSet.copyOf(permissions));
    }

    /**
     * Returns the permission of every role for a command whose permission depends on a role argument.
     *
     * @param permissionOf the permission for each role, e.g. {@link RolePermission#toSignUp(Role)}
     * @return one permission per role
     */
    public static Set<RolePermission> forEveryRole(Function<Role, RolePermission> permissionOf) {
        return Arrays.stream(Role.values()).map(permissionOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(RolePermission.class)));
    }
}
