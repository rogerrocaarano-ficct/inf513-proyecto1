package grupo03sc.mailapp.users.domain;

/**
 * Why a user account request is rejected (UC-001 E1 to E5, UC-003 E3 and E4), with the text that starts the reply.
 */
public enum RejectionReason {
    OUT_OF_SCOPE("Fuera de su alcance"),
    DUPLICATE_EMAIL("Dirección de correo repetida"),
    DUPLICATE_REGISTRATION_NUMBER("Registro universitario repetido"),
    INVALID_ROLE_COMBINATION("Combinación de roles no permitida"),
    SECOND_THESIS_WORKSHOP_PROFESSOR("Ya hay un docente de Taller de Grado activo"),
    LAST_ADMINISTRATOR("El sistema quedaría sin un administrador activo"),
    MISSING_DATA("Faltan datos"),
    INVALID_DATA("Dato inválido"),
    ACCOUNT_NOT_FOUND("La cuenta de usuario no existe"),
    INACTIVE_ACCOUNT("La cuenta de usuario está desactivada");

    private final String text;

    RejectionReason(String text) {
        this.text = text;
    }

    public String message(String detail) {
        return detail == null || detail.isBlank() ? text + "." : text + ": " + detail + ".";
    }
}
