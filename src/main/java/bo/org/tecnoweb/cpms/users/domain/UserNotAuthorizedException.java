package bo.org.tecnoweb.cpms.users.domain;

import bo.org.tecnoweb.cpms.common.DomainException;

/**
 * The sender of a command may not perform it: the address has no account, or the account's role
 * lacks the permission (UC-001, E1).
 */
public class UserNotAuthorizedException extends DomainException {

    /** Error code of every not-authorized rejection. */
    public static final String ERROR_CODE = "USER_NOT_AUTHORIZED";

    /**
     * The account exists but its role lacks the permission.
     *
     * @param email  address of the account
     * @param action what it tried to do, in Spanish, e.g. {@code crear cuentas de docente}
     */
    public UserNotAuthorizedException(String email, String action) {
        super(ERROR_CODE, "La cuenta " + email + " no tiene permiso para " + action + ".");
    }

    private UserNotAuthorizedException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * The address has no account.
     *
     * @param email the sender's address
     * @return the exception, with a message saying the address is not registered
     */
    public static UserNotAuthorizedException notRegistered(String email) {
        return new UserNotAuthorizedException("La dirección " + email + " no está registrada en el sistema. "
                + "Solicite una cuenta a la Dirección de Carrera.");
    }
}
