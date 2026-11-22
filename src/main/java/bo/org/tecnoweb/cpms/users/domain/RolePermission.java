package bo.org.tecnoweb.cpms.users.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Permission to perform an operation, separate from the role: it lists the roles
 * allowed to perform it.
 *
 * <p>The {@code SIGNUP_<role>} permissions allow creating accounts of that role, and the {@code
 * LIST_<role>} permissions listing them; each {@code LIST_} is granted to the same roles as its
 * {@code SIGNUP_}. The {@code CHANGE_STATUS_<role>} permissions allow disabling and enabling accounts
 * of that role; only directors and administrators hold them.
 */
public enum RolePermission {

    /** Open a graduation procedure (UC-002, FR-001). */
    OPEN_DEGREE_PROCEDURE("abrir trámites de titulación", Role.ADMINISTRATIVE),

    /** Replace an initial document (UC-002, FR-055). */
    REPLACE_INITIAL_DOCUMENT("reemplazar documentos iniciales", Role.ADMINISTRATIVE),

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
    SIGNUP_SYSTEM_ADMIN("crear cuentas de administrador", Role.SYSTEM_ADMIN),

    /** List student accounts. */
    LIST_STUDENTS("listar cuentas de estudiante", Role.ADMINISTRATIVE, Role.DIRECTOR, Role.SYSTEM_ADMIN),

    /** List administrative staff accounts. */
    LIST_ADMINISTRATIVES("listar cuentas de administrativo", Role.DIRECTOR, Role.SYSTEM_ADMIN),

    /** List professor accounts. */
    LIST_PROFESSORS("listar cuentas de docente", Role.DIRECTOR, Role.SYSTEM_ADMIN),

    /** List the capstone project professor accounts. */
    LIST_CAPSTONE_PROJECT_PROFESSORS("listar cuentas de docente de Taller de Grado", Role.DIRECTOR,
            Role.SYSTEM_ADMIN),

    /** List director accounts. */
    LIST_DIRECTORS("listar cuentas de director", Role.SYSTEM_ADMIN),

    /** List system administrator accounts. */
    LIST_ADMINS("listar cuentas de administrador", Role.SYSTEM_ADMIN),

    /** Disable and enable student accounts. */
    CHANGE_STATUS_STUDENTS("cambiar el estado de cuentas de estudiante", Role.DIRECTOR, Role.SYSTEM_ADMIN),

    /** Disable and enable administrative staff accounts. */
    CHANGE_STATUS_ADMINISTRATIVES("cambiar el estado de cuentas de administrativo", Role.DIRECTOR,
            Role.SYSTEM_ADMIN),

    /** Disable and enable professor accounts. */
    CHANGE_STATUS_PROFESSORS("cambiar el estado de cuentas de docente", Role.DIRECTOR, Role.SYSTEM_ADMIN),

    /** Disable and enable the capstone project professor accounts. */
    CHANGE_STATUS_CAPSTONE_PROJECT_PROFESSORS("cambiar el estado de cuentas de docente de Taller de Grado",
            Role.DIRECTOR, Role.SYSTEM_ADMIN),

    /** Disable and enable director accounts. */
    CHANGE_STATUS_DIRECTORS("cambiar el estado de cuentas de director", Role.SYSTEM_ADMIN),

    /** Disable and enable system administrator accounts. */
    CHANGE_STATUS_ADMINS("cambiar el estado de cuentas de administrador", Role.SYSTEM_ADMIN);

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
     * Returns the permission needed to list the accounts of a role (UC-001 A3).
     *
     * @param role the role of the accounts to list
     * @return the matching {@code LIST_} permission, granted to the same roles as its {@code SIGNUP_}
     */
    public static RolePermission toList(Role role) {
        return switch (role) {
            case STUDENT -> LIST_STUDENTS;
            case ADMINISTRATIVE -> LIST_ADMINISTRATIVES;
            case PROFESSOR -> LIST_PROFESSORS;
            case CAPSTONE_PROJECT_PROFESSOR -> LIST_CAPSTONE_PROJECT_PROFESSORS;
            case DIRECTOR -> LIST_DIRECTORS;
            case SYSTEM_ADMIN -> LIST_ADMINS;
        };
    }

    /**
     * Returns the permission needed to disable or enable accounts of a role (UC-001 A2).
     *
     * @param role the role of the affected account
     * @return the matching {@code CHANGE_STATUS_} permission
     */
    public static RolePermission toChangeStatus(Role role) {
        return switch (role) {
            case STUDENT -> CHANGE_STATUS_STUDENTS;
            case ADMINISTRATIVE -> CHANGE_STATUS_ADMINISTRATIVES;
            case PROFESSOR -> CHANGE_STATUS_PROFESSORS;
            case CAPSTONE_PROJECT_PROFESSOR -> CHANGE_STATUS_CAPSTONE_PROJECT_PROFESSORS;
            case DIRECTOR -> CHANGE_STATUS_DIRECTORS;
            case SYSTEM_ADMIN -> CHANGE_STATUS_ADMINS;
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
