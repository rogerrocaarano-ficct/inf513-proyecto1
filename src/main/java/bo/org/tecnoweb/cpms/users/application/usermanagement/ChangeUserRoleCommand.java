package bo.org.tecnoweb.cpms.users.application.usermanagement;

/**
 * Role change of an account (UC-001 A1, role only, FR-031). Each component is one argument of the email
 * command {@code usuarios.cambiarRol[userEmail; role]}.
 *
 * @param userEmail email address of the account whose role changes
 * @param role      label of the new role, e.g. {@code docente}
 */
public record ChangeUserRoleCommand(String userEmail, String role) {
}
