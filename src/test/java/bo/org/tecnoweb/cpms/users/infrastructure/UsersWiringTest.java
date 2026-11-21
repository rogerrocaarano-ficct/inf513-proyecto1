package bo.org.tecnoweb.cpms.users.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import bo.org.tecnoweb.cpms.TestcontainersConfiguration;
import bo.org.tecnoweb.cpms.mail.application.EmailCommandHandler;
import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * The application starts with the users subsystem wired: its handler joins the mail channel and the
 * configured administrator exists.
 */
@SpringBootTest(properties = "cpms.users.admin-email=admin@cpms.test")
@Import(TestcontainersConfiguration.class)
class UsersWiringTest {

    @Autowired
    private List<EmailCommandHandler> handlers;

    @Autowired
    private UserRepository users;

    @Test
    void handlersAreRegisteredInTheMailChannel() {
        assertThat(handlers).extracting(EmailCommandHandler::actionKey)
                .contains("usuarios.crearCuenta", "usuarios.listar");
    }

    @Test
    void configuredAdministratorExistsAfterStartup() {
        assertThat(users.getByEmail("admin@cpms.test")).get()
                .satisfies(admin -> assertThat(admin.getRole()).isEqualTo(Role.SYSTEM_ADMIN));
    }
}
