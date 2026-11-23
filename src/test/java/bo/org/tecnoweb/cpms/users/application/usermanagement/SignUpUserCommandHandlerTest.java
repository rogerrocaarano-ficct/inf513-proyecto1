package bo.org.tecnoweb.cpms.users.application.usermanagement;

import static org.assertj.core.api.Assertions.assertThat;

import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import org.junit.jupiter.api.Test;

class SignUpUserCommandHandlerTest {

    private String requester;
    private SignUpUserCommand received;

    private final SignUpUserCommandHandler handler = new SignUpUserCommandHandler(
            new UserManagementUseCase(null, null) {
                @Override
                public void signUpUser(String requesterEmail, SignUpUserCommand command) {
                    requester = requesterEmail;
                    received = command;
                }
            });

    @Test
    void declaresTheCommandContract() {
        assertThat(handler.actionKey()).isEqualTo("usuarios.crearCuenta");
        assertThat(handler.requiredArgs()).isEqualTo(8);
        assertThat(handler.requiredFiles()).isZero();
    }

    @Test
    void eachArgumentReachesItsFieldAndTheSenderIsTheRequester() {
        ReceivedEmail email = ReceivedEmail.builder()
                .fromSender("director@uagrm.bo")
                .withSubject("usuarios.crearCuenta[Ana María; Pérez; Rojas; 1234567; 218001234; ana@uagrm.bo; "
                        + "71234567; estudiante]");

        handler.handle(email);

        assertThat(requester).isEqualTo("director@uagrm.bo");
        assertThat(received).isEqualTo(new SignUpUserCommand("Ana María", "Pérez", "Rojas", "estudiante",
                "1234567", "218001234", "ana@uagrm.bo", "71234567"));
    }
}
