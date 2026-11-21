package bo.org.tecnoweb.cpms.users.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Permission to perform an operation on user accounts, separate from the role: it lists the roles
 * allowed to perform it.
 *
 * <p>The {@code SIGNUP_<role>} permissions allow creating accounts of that role.
 */
public enum RolePermission {

    /** Create student accounts. */
    SIGNUP_STUDENT("crear cuentas de estudiante", Role.ADMINISTRATIVE, Role.DIRECTOR, Role.SYSTEM_ADMIN),

    /** Create administrative staff accounts. */
    SIGNUP_ADMINISTRATIVE("crear cuentas de administrativo", Role.DIRECTOR, Role.SYSTEM_ADMIN),

    /** Create professor accounts. */
    SIGNUP_PROFESSOR("crear cuentas de docente", Role.DIRECTOR, Role.SYSTEM_ADMIN),

    /** Create the capstone project professor account. */
    SIGNUP_CAPSTONE_PROJECT_PROFESSOR("crear cuentas de docente de Taller de Grado", Role.DIRECTOR,
            Role.SYSTEM_ADMIN),

    /** Create director accounts. */
    SIGNUP_DIRECTOR("crear cuentas de director", Role.SYSTEM_ADMIN),

    /** Create system administrator accounts. */
    SIGNUP_SYSTEM_ADMIN("crear cuentas de administrador", Role.SYSTEM_ADMIN);

    private final String label;
    private final Set<Role> allowedRoles;

    RolePermission(String label, Role first, Role... rest) {
        this.label = label;
        this.allowedRoles = EnumSet.of(first, rest);
    }

    /**
     * Returns the permission needed to create accounts of a role.
     *
     * @param role the role of the new account
     * @return the matching {@code SIGNUP_} permission
     */
    public static RolePermission toSignUp(Role role) {
        return switch (role) {
            case STUDENT -> SIGNUP_STUDENT;
            case ADMINISTRATIVE -> SIGNUP_ADMINISTRATIVE;
            case PROFESSOR -> SIGNUP_PROFESSOR;
            case CAPSTONE_PROJECT_PROFESSOR -> SIGNUP_CAPSTONE_PROJECT_PROFESSOR;
            case DIRECTOR -> SIGNUP_DIRECTOR;
            case SYSTEM_ADMIN -> SIGNUP_SYSTEM_ADMIN;
        };
    }

    /**
     * Returns what the permission allows, in Spanish, for messages to users.
     *
     * @return the description, e.g. {@code crear cuentas de docente}
     */
    public String getLabel() {
        return label;
    }

    /**
     * Tells whether a role holds this permission.
     *
     * @param userRole the role to check
     * @return {@code true} if the role is one of the allowed roles
     */
    public boolean isGrantedTo(Role userRole) {
        return allowedRoles.contains(userRole);
    }
}
