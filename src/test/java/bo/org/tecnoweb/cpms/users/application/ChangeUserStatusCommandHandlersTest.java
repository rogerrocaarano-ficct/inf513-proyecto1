package bo.org.tecnoweb.cpms.users.application;

import static org.assertj.core.api.Assertions.assertThat;

import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** {@link DisableUserCommandHandler} and {@link EnableUserCommandHandler}. */
class ChangeUserStatusCommandHandlersTest {

    private final List<String> calls = new ArrayList<>();

    private final UserManagementUseCase useCase = new UserManagementUseCase(null, null) {
        @Override
        public void disableUser(String requesterEmail, String userEmail) {
            calls.add("disable " + requesterEmail + " " + userEmail);
        }

        @Override
        public void enableUser(String requesterEmail, String userEmail) {
            calls.add("enable " + requesterEmail + " " + userEmail);
        }
    };

    private static ReceivedEmail email(String subject) {
        return ReceivedEmail.builder().fromSender("director@uagrm.bo").withSubject(subject);
    }

    @Test
    void disableDeclaresTheCommandContract() {
        DisableUserCommandHandler handler = new DisableUserCommandHandler(useCase);

        assertThat(handler.actionKey()).isEqualTo("usuarios.desactivar");
        assertThat(handler.requiredArgs()).isEqualTo(1);
        assertThat(handler.requiredFiles()).isZero();
    }

    @Test
    void enableDeclaresTheCommandContract() {
        EnableUserCommandHandler handler = new EnableUserCommandHandler(useCase);

        assertThat(handler.actionKey()).isEqualTo("usuarios.activar");
        assertThat(handler.requiredArgs()).isEqualTo(1);
        assertThat(handler.requiredFiles()).isZero();
    }

    @Test
    void theArgumentIsTheAffectedAccountAndTheSenderIsTheRequester() {
        new DisableUserCommandHandler(useCase).handle(email("usuarios.desactivar[ luis@uagrm.bo ]"));
        new EnableUserCommandHandler(useCase).handle(email("usuarios.activar[luis@uagrm.bo]"));

        assertThat(calls).containsExactly(
                "disable director@uagrm.bo luis@uagrm.bo",
                "enable director@uagrm.bo luis@uagrm.bo");
    }
}
