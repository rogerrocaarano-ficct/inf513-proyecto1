package bo.org.tecnoweb.cpms.users.application;

/**
 * Data of a new account (UC-001, basic flow). Each component is one argument of the email command
 * {@code usuarios.crearCuenta[firstName; lastName; secondLastName; ci; code; email; phoneNumber; role]}.
 *
 * @param firstName      first name or names
 * @param lastName       first surname
 * @param secondLastName second surname; may be empty
 * @param role           label of the role, e.g. {@code docente}
 * @param ci             identity card number; may be empty except for students
 * @param code           code in the university's system; may be empty except for students
 * @param email          email address of the new account
 * @param phoneNumber    phone number; may be empty except for students
 */
public record SignUpUserCommand(
        String firstName,
        String lastName,
        String secondLastName,
        String role,
        String ci,
        String code,
        String email,
        String phoneNumber) {
}
