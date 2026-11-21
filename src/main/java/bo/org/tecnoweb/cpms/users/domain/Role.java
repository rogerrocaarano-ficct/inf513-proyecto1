package bo.org.tecnoweb.cpms.users.domain;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Role of a user account. Each account has exactly one role.
 *
 * <p>Roles are listed in ascending order. What a role can do is not derived from that order: it is
 * defined by each {@link RolePermission} through its allowed roles.
 */
public enum Role {

    /** Student who carries out the graduation procedure. */
    STUDENT("estudiante"),

    /** Staff of the career's office (secretario(a) de dirección de carrera). */
    ADMINISTRATIVE("administrativo"),

    /** Professor of the career. */
    PROFESSOR("docente"),

    /** The single professor of the capstone project course (docente de Taller de Grado). */
    CAPSTONE_PROJECT_PROFESSOR("docente_taller"),

    /** Head of the career (director de carrera). */
    DIRECTOR("director"),

    /** Technical administrator of the system. */
    SYSTEM_ADMIN("administrador");

    /** Field name, in Spanish, used in validation errors about the role. */
    public static final String FIELD = "rol";

    private final String label;

    Role(String label) {
        this.label = label;
    }

    /**
     * Returns the label users write in email commands.
     *
     * @return the label, in Spanish and lower case
     */
    public String getLabel() {
        return label;
    }

    /**
     * Finds the role of a label, without case.
     *
     * @param label the label written by the user, e.g. {@code Docente}
     * @return the role with that label
     * @throws DomainValidationException if no role has that label; the error lists the accepted labels
     */
    public static Role fromLabel(String label) {
        String wanted = label == null ? "" : label.strip().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(role -> role.label.equals(wanted))
                .findFirst()
                .orElseThrow(() -> new DomainValidationException(Map.of(FIELD,
                        "\"" + (label == null ? "" : label.strip()) + "\" no es un rol válido. Roles aceptados: "
                                + Arrays.stream(values()).map(Role::getLabel).collect(Collectors.joining(", "))
                                + ".")));
    }

    /**
     * Tells whether this role may perform an operation.
     *
     * @param permission the permission the operation requires
     * @return {@code true} if the permission allows this role
     */
    public boolean isAllowedTo(RolePermission permission) {
        return permission.isGrantedTo(this);
    }

    /**
     * Returns the permissions granted to this role.
     *
     * @return the permissions whose allowed roles include this role, in declaration order
     */
    public List<RolePermission> getPermissions() {
        return Arrays.stream(RolePermission.values()).filter(this::isAllowedTo).toList();
    }
}
