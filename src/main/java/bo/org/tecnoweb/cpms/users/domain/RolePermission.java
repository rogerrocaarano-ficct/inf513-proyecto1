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
 * of that role, and the {@code CHANGE_ROLE_<role>} permissions changing the role of accounts of that
 * role or giving that role; only directors and administrators hold either.
 */
public enum RolePermission {

    /** Open a graduation procedure (UC-002, FR-001). */
    OPEN_DEGREE_PROCEDURE("abrir trámites de titulación", Role.ADMINISTRATIVE),

    /** Replace the document of an opening requirement (UC-002, FR-055). */
    REPLACE_OPENING_REQUIREMENT("reemplazar requisitos de apertura", Role.ADMINISTRATIVE),

    /** Submit the project profile or a new version of it (UC-004, FR-004, FR-023). */
    REGISTER_PROJECT_PROFILE("presentar el perfil de proyecto", Role.STUDENT),

    /** Approve, observe or reject a project profile (UC-004, FR-005, FR-022, FR-039). */
    EVALUATE_PROJECT_PROFILE("evaluar perfiles de proyecto", Role.CAPSTONE_PROJECT_PROFESSOR),

    /** Assign the project tutor (UC-003, FR-003). */
    ASSIGN_TUTOR("asignar un tutor", Role.DIRECTOR),

    /** Be eligible for the project tutor designation (UC-003, FR-003). */
    BE_PROJECT_TUTOR("ser tutor de proyecto", Role.PROFESSOR, Role.CAPSTONE_PROJECT_PROFESSOR, Role.DIRECTOR),

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
    CHANGE_STATUS_ADMINS("cambiar el estado de cuentas de administrador", Role.SYSTEM_ADMIN),

    /** Change the role of student accounts, or give the student role (UC-001 A1, FR-031). */
    CHANGE_ROLE_STUDENTS("cambiar el rol de cuentas de estudiante", Role.DIRECTOR, Role.SYSTEM_ADMIN),

    /** Change the role of administrative staff accounts, or give that role (UC-001 A1, FR-031). */
    CHANGE_ROLE_ADMINISTRATIVES("cambiar el rol de cuentas de administrativo", Role.DIRECTOR, Role.SYSTEM_ADMIN),

    /** Change the role of professor accounts, or give that role (UC-001 A1, FR-031). */
    CHANGE_ROLE_PROFESSORS("cambiar el rol de cuentas de docente", Role.DIRECTOR, Role.SYSTEM_ADMIN),

    /** Change the role of capstone project professor accounts, or give that role (UC-001 A1, FR-031). */
    CHANGE_ROLE_CAPSTONE_PROJECT_PROFESSORS("cambiar el rol de cuentas de docente de Taller de Grado",
            Role.DIRECTOR, Role.SYSTEM_ADMIN),

    /** Change the role of director accounts, or give that role (UC-001 A1, FR-031, FR-053). */
    CHANGE_ROLE_DIRECTORS("cambiar el rol de cuentas de director", Role.SYSTEM_ADMIN),

    /** Change the role of system administrator accounts, or give that role (UC-001 A1, FR-031, FR-053). */
    CHANGE_ROLE_ADMINS("cambiar el rol de cuentas de administrador", Role.SYSTEM_ADMIN);

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
     * Returns the permission needed to change the role of accounts of a role, or to give an account that
     * role (UC-001 A1, FR-031). A role change needs the permission of the current role and of the new one.
     *
     * @param role the current or the new role of the affected account
     * @return the matching {@code CHANGE_ROLE_} permission
     */
    public static RolePermission toChangeRole(Role role) {
        return switch (role) {
            case STUDENT -> CHANGE_ROLE_STUDENTS;
            case ADMINISTRATIVE -> CHANGE_ROLE_ADMINISTRATIVES;
            case PROFESSOR -> CHANGE_ROLE_PROFESSORS;
            case CAPSTONE_PROJECT_PROFESSOR -> CHANGE_ROLE_CAPSTONE_PROJECT_PROFESSORS;
            case DIRECTOR -> CHANGE_ROLE_DIRECTORS;
            case SYSTEM_ADMIN -> CHANGE_ROLE_ADMINS;
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
