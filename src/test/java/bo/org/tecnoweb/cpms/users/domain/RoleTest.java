package bo.org.tecnoweb.cpms.users.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RoleTest {

    @ParameterizedTest
    @CsvSource({
            "estudiante, STUDENT",
            "Administrativo, ADMINISTRATIVE",
            "DOCENTE, PROFESSOR",
            "docente_taller, CAPSTONE_PROJECT_PROFESSOR",
            " director , DIRECTOR",
            "administrador, SYSTEM_ADMIN"})
    void fromLabelIgnoresCaseAndSpaces(String label, Role expected) {
        assertThat(Role.fromLabel(label)).isEqualTo(expected);
    }

    @Test
    void unknownLabelIsAValidationErrorThatListsTheAcceptedRoles() {
        assertThatThrownBy(() -> Role.fromLabel("rector"))
                .isInstanceOfSatisfying(DomainValidationException.class, e -> assertThat(e.getErrors())
                        .containsOnlyKeys(Role.FIELD)
                        .extractingByKey(Role.FIELD).asString()
                        .contains("\"rector\"")
                        .contains("estudiante, administrativo, docente, docente_taller, director, administrador"));
    }

    /** "Quién puede crear cada cuenta": rows are who creates, columns the roles they may create. */
    @Test
    void signUpPermissionsFollowTheAgreedTable() {
        assertCreates(Role.SYSTEM_ADMIN, EnumSet.allOf(Role.class));
        assertCreates(Role.DIRECTOR, EnumSet.of(Role.PROFESSOR, Role.CAPSTONE_PROJECT_PROFESSOR,
                Role.ADMINISTRATIVE, Role.STUDENT));
        assertCreates(Role.ADMINISTRATIVE, EnumSet.of(Role.STUDENT));
        assertCreates(Role.PROFESSOR, EnumSet.noneOf(Role.class));
        assertCreates(Role.CAPSTONE_PROJECT_PROFESSOR, EnumSet.noneOf(Role.class));
        assertCreates(Role.STUDENT, EnumSet.noneOf(Role.class));
    }

    private static void assertCreates(Role creator, Set<Role> created) {
        for (Role newRole : Role.values()) {
            assertThat(creator.isAllowedTo(RolePermission.toSignUp(newRole)))
                    .as("%s creates %s", creator, newRole)
                    .isEqualTo(created.contains(newRole));
        }
    }

    /** "Quién puede listar cada rol": rows are who lists, columns the roles they may list. */
    @Test
    void listPermissionsFollowTheAgreedTable() {
        assertLists(Role.SYSTEM_ADMIN, EnumSet.allOf(Role.class));
        assertLists(Role.DIRECTOR, EnumSet.of(Role.PROFESSOR, Role.CAPSTONE_PROJECT_PROFESSOR,
                Role.ADMINISTRATIVE, Role.STUDENT));
        assertLists(Role.ADMINISTRATIVE, EnumSet.of(Role.STUDENT));
        assertLists(Role.PROFESSOR, EnumSet.noneOf(Role.class));
        assertLists(Role.CAPSTONE_PROJECT_PROFESSOR, EnumSet.noneOf(Role.class));
        assertLists(Role.STUDENT, EnumSet.noneOf(Role.class));
    }

    private static void assertLists(Role lister, Set<Role> listed) {
        for (Role role : Role.values()) {
            assertThat(lister.isAllowedTo(RolePermission.toList(role)))
                    .as("%s lists %s", lister, role)
                    .isEqualTo(listed.contains(role));
        }
    }

    @Test
    void eachListPermissionHasTheRolesOfItsSignUpPermission() {
        for (Role role : Role.values()) {
            for (Role requester : Role.values()) {
                assertThat(RolePermission.toList(role).isGrantedTo(requester))
                        .as("%s lists/creates %s", requester, role)
                        .isEqualTo(RolePermission.toSignUp(role).isGrantedTo(requester));
            }
        }
    }

    /** "Quién puede desactivar y activar cada cuenta": rows are who changes, columns the affected roles. */
    @Test
    void changeStatusPermissionsFollowTheAgreedTable() {
        assertChangesStatus(Role.SYSTEM_ADMIN, EnumSet.allOf(Role.class));
        assertChangesStatus(Role.DIRECTOR, EnumSet.of(Role.PROFESSOR, Role.CAPSTONE_PROJECT_PROFESSOR,
                Role.ADMINISTRATIVE, Role.STUDENT));
        assertChangesStatus(Role.ADMINISTRATIVE, EnumSet.noneOf(Role.class));
        assertChangesStatus(Role.PROFESSOR, EnumSet.noneOf(Role.class));
        assertChangesStatus(Role.CAPSTONE_PROJECT_PROFESSOR, EnumSet.noneOf(Role.class));
        assertChangesStatus(Role.STUDENT, EnumSet.noneOf(Role.class));
    }

    private static void assertChangesStatus(Role requester, Set<Role> affected) {
        for (Role role : Role.values()) {
            assertThat(requester.isAllowedTo(RolePermission.toChangeStatus(role)))
                    .as("%s changes the status of %s", requester, role)
                    .isEqualTo(affected.contains(role));
        }
    }

    @Test
    void changeStatusPermissionLabelNamesTheRole() {
        assertThat(RolePermission.toChangeStatus(Role.DIRECTOR).getLabel())
                .isEqualTo("cambiar el estado de cuentas de director");
    }

    @Test
    void permissionsOfARoleAreThoseThatIncludeIt() {
        assertThat(Role.ADMINISTRATIVE.getPermissions())
                .containsExactly(RolePermission.OPEN_DEGREE_PROCEDURE, RolePermission.REPLACE_INITIAL_DOCUMENT,
                        RolePermission.SIGNUP_STUDENT, RolePermission.LIST_STUDENTS);
        assertThat(Role.STUDENT.getPermissions()).containsExactly(RolePermission.REGISTER_INITIAL_PROFILE);
        assertThat(Role.SYSTEM_ADMIN.getPermissions())
                .doesNotContain(RolePermission.OPEN_DEGREE_PROCEDURE, RolePermission.REPLACE_INITIAL_DOCUMENT,
                        RolePermission.REGISTER_INITIAL_PROFILE, RolePermission.ASSIGN_TUTOR,
                        RolePermission.BE_PROJECT_TUTOR)
                .hasSize(RolePermission.values().length - 5);
    }
}
