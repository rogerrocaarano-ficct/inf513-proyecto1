package bo.org.tecnoweb.cpms.users.application;

/**
 * One account in the list of {@code usuarios.listar[role]} (UC-001 A3).
 *
 * <p>The email address identifies the account in the commands that change it; the internal
 * identifier is not listed.
 *
 * @param fullName     names, first surname and second surname, if any
 * @param phoneNumber  phone number, or {@code null} if it has none
 * @param code         code in the university's system, or {@code null} if it has none
 * @param emailAddress email address of the account
 * @param role         label of the role, e.g. {@code docente}
 * @param ci           identity card number, or {@code null} if it has none
 * @param active       {@code true} if the account is active; disabled accounts are listed too
 */
public record ListUsersQueryItem(
        String fullName,
        String phoneNumber,
        String code,
        String emailAddress,
        String role,
        String ci,
        boolean active) {
}
