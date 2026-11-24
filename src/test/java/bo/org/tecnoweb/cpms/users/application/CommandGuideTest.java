package bo.org.tecnoweb.cpms.users.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import bo.org.tecnoweb.cpms.users.domain.RolePermission;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Checks the description of a command for the help reply (FR-066). */
class CommandGuideTest {

    @Test
    void keepsAnUnmodifiableCopyOfThePermissionsInDeclarationOrder() {
        var permissions = new HashSet<>(Set.of(RolePermission.LIST_ADMINS, RolePermission.LIST_STUDENTS));
        var guide = new CommandGuide("usuarios.listar", "usuarios.listar[rol]", "Listar.", "", permissions);
        permissions.clear();

        assertThat(guide.permissions())
                .containsExactly(RolePermission.LIST_STUDENTS, RolePermission.LIST_ADMINS);
        assertThatThrownBy(() -> guide.permissions().add(RolePermission.LIST_DIRECTORS))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void acceptsNoPermissionsForACommandEveryoneMayUse() {
        var guide = new CommandGuide("ayuda", "ayuda[]", "Ver.", "", Set.of());

        assertThat(guide.permissions()).isEmpty();
    }

    @Test
    void rejectsNullPermissions() {
        assertThatThrownBy(() -> new CommandGuide("a", "a[]", "A.", "", null))
                .isInstanceOf(NullPointerException.class);
        var withNull = new HashSet<RolePermission>();
        withNull.add(null);
        assertThatThrownBy(() -> new CommandGuide("a", "a[]", "A.", "", withNull))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void forEveryRoleListsOnePermissionPerRole() {
        assertThat(CommandGuide.forEveryRole(RolePermission::toSignUp)).containsExactly(
                RolePermission.SIGNUP_STUDENT, RolePermission.SIGNUP_ADMINISTRATIVE,
                RolePermission.SIGNUP_PROFESSOR, RolePermission.SIGNUP_CAPSTONE_PROJECT_PROFESSOR,
                RolePermission.SIGNUP_DIRECTOR, RolePermission.SIGNUP_SYSTEM_ADMIN);
    }
}
