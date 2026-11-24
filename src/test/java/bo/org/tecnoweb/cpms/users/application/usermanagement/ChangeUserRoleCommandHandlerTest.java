package bo.org.tecnoweb.cpms.users.application.usermanagement;

import static org.assertj.core.api.Assertions.assertThat;

import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.RolePermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/** {@link ChangeUserRoleCommandHandler} (UC-001 A1, role only). */
class ChangeUserRoleCommandHandlerTest {

    private final List<String> calls = new ArrayList<>();

    private final UserManagementUseCase useCase = new UserManagementUseCase(null, null) {
        @Override
        public void changeUserRole(String requesterEmail, ChangeUserRoleCommand command) {
            calls.add(requesterEmail + " " + command.userEmail() + " " + command.role());
        }
    };

    private final ChangeUserRoleCommandHandler handler = new ChangeUserRoleCommandHandler(useCase);

    @Test
    void declaresTheCommandContract() {
        assertThat(handler.actionKey()).isEqualTo("usuarios.cambiarRol");
        assertThat(handler.requiredArgs()).isEqualTo(2);
        assertThat(handler.requiredFiles()).isZero();
    }

    @Test
    void theArgumentsAreTheAccountAndItsRoleAndTheSenderIsTheRequester() {
        handler.handle(ReceivedEmail.builder().fromSender("director@uagrm.bo")
                .withSubject("usuarios.cambiarRol[ luis@uagrm.bo ; docente_taller ]"));

        assertThat(calls).containsExactly("director@uagrm.bo luis@uagrm.bo docente_taller");
    }

    @Test
    void describesTheCommandForTheHelpReply() {
        var guide = handler.guide();

        assertThat(guide.action()).isEqualTo(ChangeUserRoleCommandHandler.ACTION);
        assertThat(guide.syntax()).isEqualTo("usuarios.cambiarRol[correo; rol]");
        assertThat(guide.permissions()).containsExactlyElementsOf(
                Arrays.stream(Role.values()).map(RolePermission::toChangeRole).toList());
    }
}
