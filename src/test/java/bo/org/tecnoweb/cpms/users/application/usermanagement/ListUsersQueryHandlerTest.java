package bo.org.tecnoweb.cpms.users.application.usermanagement;

import static org.assertj.core.api.Assertions.assertThat;

import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import org.junit.jupiter.api.Test;

class ListUsersQueryHandlerTest {

    private String requester;
    private ListUsersQuery received;

    private final ListUsersQueryHandler handler = new ListUsersQueryHandler(
            new UserManagementUseCase(null, null) {
                @Override
                public void listUsers(String requesterEmail, ListUsersQuery query) {
                    requester = requesterEmail;
                    received = query;
                }
            });

    @Test
    void declaresTheCommandContract() {
        assertThat(handler.actionKey()).isEqualTo("usuarios.listar");
        assertThat(handler.requiredArgs()).isEqualTo(1);
        assertThat(handler.requiredFiles()).isZero();
    }

    @Test
    void theArgumentIsTheRoleAndTheSenderIsTheRequester() {
        ReceivedEmail email = ReceivedEmail.builder()
                .fromSender("secretaria@uagrm.bo")
                .withSubject("usuarios.listar[ Estudiante ]");

        handler.handle(email);

        assertThat(requester).isEqualTo("secretaria@uagrm.bo");
        assertThat(received).isEqualTo(new ListUsersQuery("Estudiante"));
    }
}
