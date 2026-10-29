package grupo03sc.mailapp.users.domain;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The fields of a user account that a manager can change. The label is the Spanish name used in
 * commands and replies; the property is the name recorded in the change log.
 */
public enum AccountField {
    FULL_NAME("nombre", "fullName"),
    EMAIL("correo", "email"),
    ROLES("roles", "roles"),
    STUDENT_REGISTRATION_NUMBER("registro", "studentRegistrationNumber"),
    ID_CARD("cedula", "idCard"),
    PHONE("telefono", "phone");

    private final String label;
    private final String property;

    AccountField(String label, String property) {
        this.label = label;
        this.property = property;
    }

    public String label() {
        return label;
    }

    public String property() {
        return property;
    }

    /** The field with this label, ignoring case and surrounding spaces. */
    public static Optional<AccountField> fromLabel(String text) {
        String normalized = text.strip().toLowerCase(Locale.ROOT);
        return Arrays.stream(values()).filter(field -> field.label.equals(normalized)).findFirst();
    }

    public static String labels() {
        return Arrays.stream(values()).map(AccountField::label).collect(Collectors.joining(", "));
    }
}
