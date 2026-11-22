package bo.org.tecnoweb.cpms.users.application;

/**
 * Data of the email command {@code usuarios.listar[role]} (UC-001 A3).
 *
 * @param role label of the role as the user wrote it, e.g. {@code Docente}; compared without case
 */
public record ListUsersQuery(String role) {
}
